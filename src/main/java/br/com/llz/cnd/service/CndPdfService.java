package br.com.llz.cnd.service;

import br.com.llz.cnd.dto.UnidadeCndData;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class CndPdfService {
    
    @Value("${cnd.url.validacao}")
    private String urlValidacao;
    
    @Value("${company.name}")
    private String companyName;
    
    @Value("${company.address}")
    private String companyAddress;
    
    @Value("${company.neighborhood}")
    private String companyNeighborhood;
    
    @Value("${company.city}")
    private String companyCity;
    
    @Value("${company.state}")
    private String companyState;
    
    @Value("${company.zipcode}")
    private String companyZipcode;
    
    @Value("${company.phone}")
    private String companyPhone;
    
    @Value("${company.email}")
    private String companyEmail;
    
    public byte[] gerarPdf(UnidadeCndData dados, String codigo) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            
            // Fontes
            PdfFont fontRegular = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            
            // Header com logo
            adicionarHeader(document, fontBold);
            
            // Código de validação
            adicionarCodigoValidacao(document, codigo, fontRegular);
            
            // Conteúdo principal
            adicionarConteudo(document, dados, fontRegular, fontBold);
            
            // Dados da empresa
            adicionarDadosEmpresa(document, fontRegular);
            
            // QR Code
            adicionarQrCode(document, codigo);
            
            // Hash do documento
            adicionarHashDocumento(document, fontRegular);
            
            document.close();
            return baos.toByteArray();
            
        } catch (Exception e) {
            log.error("Erro ao gerar PDF da CND: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao gerar PDF da CND", e);
        }
    }
    
    private void adicionarHeader(Document document, PdfFont fontBold) throws IOException {
        // Logo
        try {
            ClassPathResource logoResource = new ClassPathResource("static/llz.png");
            ImageData logoData = ImageDataFactory.create(logoResource.getURL());
            Image logo = new Image(logoData);
            logo.setWidth(150);
            logo.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER);
            document.add(logo);
        } catch (Exception e) {
            log.warn("Erro ao carregar logo: {}", e.getMessage());
        }
        
        // Título
        Paragraph titulo = new Paragraph("CERTIDÃO NEGATIVA DE DÉBITOS")
            .setFont(fontBold)
            .setFontSize(20)
            .setTextAlignment(TextAlignment.CENTER)
            .setFontColor(ColorConstants.DARK_GRAY)
            .setMarginTop(20)
            .setMarginBottom(30);
        document.add(titulo);
    }
    
    private void adicionarCodigoValidacao(Document document, String codigo, PdfFont fontRegular) {
        Paragraph codigoP = new Paragraph("Código de Validação: " + codigo)
            .setFont(fontRegular)
            .setFontSize(12)
            .setTextAlignment(TextAlignment.RIGHT)
            .setFontColor(ColorConstants.BLUE)
            .setMarginBottom(20);
        document.add(codigoP);
    }
    
    private void adicionarConteudo(Document document, UnidadeCndData dados, PdfFont fontRegular, PdfFont fontBold) {
        // Informações da unidade
        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}));
        infoTable.setWidth(UnitValue.createPercentValue(100));
        
        infoTable.addCell(new Paragraph("Condomínio:").setFont(fontBold));
        infoTable.addCell(new Paragraph(dados.getNomeCondominio()).setFont(fontRegular));
        
        infoTable.addCell(new Paragraph("Endereço:").setFont(fontBold));
        String enderecoCompleto = String.format("%s, %s - %s, %s/%s - CEP: %s",
            dados.getLogradouro(), dados.getNumero(), dados.getBairro(),
            dados.getCidade(), dados.getUf(), dados.getCep());
        infoTable.addCell(new Paragraph(enderecoCompleto).setFont(fontRegular));
        
        infoTable.addCell(new Paragraph("Unidade:").setFont(fontBold));
        String unidadeInfo = dados.getBloco() != null ? 
            String.format("Bloco %s - Unidade %s", dados.getBloco(), dados.getUnidadeCodigo()) :
            String.format("Unidade %s", dados.getUnidadeCodigo());
        infoTable.addCell(new Paragraph(unidadeInfo).setFont(fontRegular));
        
        document.add(infoTable);
        
        // Declaração principal
        Paragraph declaracao = new Paragraph()
            .setFont(fontRegular)
            .setFontSize(14)
            .setTextAlignment(TextAlignment.JUSTIFIED)
            .setMarginTop(30)
            .setMarginBottom(20);
        
        declaracao.add("Certificamos que a unidade acima identificada ");
        declaracao.add(new Paragraph("NÃO POSSUI DÉBITOS").setFont(fontBold).setFontColor(ColorConstants.GREEN));
        declaracao.add(" em aberto junto a esta administradora na data de emissão deste documento.");
        
        document.add(declaracao);
        
        // Período (se aplicável)
        if (dados.getComPeriodo() && dados.getDataVencimento() != null && !dados.getDataVencimento().isEmpty()) {
            Paragraph periodoTitulo = new Paragraph("Período Verificado:")
                .setFont(fontBold)
                .setMarginTop(20);
            document.add(periodoTitulo);
            
            for (String data : dados.getDataVencimento()) {
                document.add(new Paragraph("• " + data).setFont(fontRegular).setMarginLeft(20));
            }
        }
        
        // Validade
        Paragraph validade = new Paragraph()
            .setFont(fontRegular)
            .setFontSize(12)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginTop(30)
            .setFontColor(ColorConstants.RED);
        
        validade.add("Esta certidão é válida até ");
        validade.add(new Paragraph(dados.getValidadeDocumento()).setFont(fontBold));
        
        document.add(validade);
    }
    
    private void adicionarDadosEmpresa(Document document, PdfFont fontRegular) {
        Paragraph empresaInfo = new Paragraph()
            .setFont(fontRegular)
            .setFontSize(10)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginTop(40)
            .setFontColor(ColorConstants.GRAY);
        
        empresaInfo.add(companyName + "\n");
        empresaInfo.add(companyAddress + " - " + companyNeighborhood + "\n");
        empresaInfo.add(companyCity + "/" + companyState + " - CEP: " + companyZipcode + "\n");
        empresaInfo.add("Tel: " + companyPhone + " | " + companyEmail);
        
        document.add(empresaInfo);
        
        // Data de emissão
        String dataEmissao = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        Paragraph emissao = new Paragraph("Documento emitido em: " + dataEmissao)
            .setFont(fontRegular)
            .setFontSize(10)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginTop(10)
            .setFontColor(ColorConstants.GRAY);
        
        document.add(emissao);
    }
    
    private void adicionarQrCode(Document document, String codigo) {
        try {
            String url = urlValidacao + "/" + codigo;
            BitMatrix matrix = new QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, 100, 100);
            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(matrix);
            
            ByteArrayOutputStream qrBaos = new ByteArrayOutputStream();
            ImageIO.write(qrImage, "PNG", qrBaos);
            
            ImageData imageData = ImageDataFactory.create(qrBaos.toByteArray());
            Image qrCode = new Image(imageData);
            qrCode.setFixedPosition(450, 50);
            qrCode.setWidth(80);
            qrCode.setHeight(80);
            
            document.add(qrCode);
            
            // Texto do QR Code
            Paragraph qrTexto = new Paragraph("Validar documento")
                .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))
                .setFontSize(8)
                .setFixedPosition(440, 35, 100)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GRAY);
            
            document.add(qrTexto);
            
        } catch (Exception e) {
            log.warn("Erro ao gerar QR Code: {}", e.getMessage());
        }
    }
    
    private void adicionarHashDocumento(Document document, PdfFont fontRegular) {
        // Placeholder para hash - será calculado após geração
        Paragraph hashInfo = new Paragraph("Hash do documento será calculado após assinatura")
            .setFont(fontRegular)
            .setFontSize(8)
            .setTextAlignment(TextAlignment.LEFT)
            .setFixedPosition(50, 20, 300)
            .setFontColor(ColorConstants.LIGHT_GRAY);
        
        document.add(hashInfo);
    }
    
    public String calcularHashDocumento(byte[] pdf) {
        return DigestUtils.sha256Hex(pdf);
    }
}