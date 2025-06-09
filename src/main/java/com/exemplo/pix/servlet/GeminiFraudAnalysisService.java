package com.exemplo.pix.servlet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GeminiFraudAnalysisService {

    // IMPORTANTE: Substitua pela sua chave de API que você gerou!
    private static final String API_KEY = "SUA_CHAVE_DE_API_AQUI";
    private static final String MODEL_NAME = "gemini-1.5-flash-001";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/" + MODEL_NAME + ":generateContent?key=" + API_KEY;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String analisarTransacao(int idContaOrigem, String chavePixDestino, BigDecimal valor, String historicoConta) throws IOException, InterruptedException {
        
        String promptText = construirPrompt(idContaOrigem, chavePixDestino, valor, historicoConta);
        
        String requestBody = String.format("{\"contents\":[{\"parts\":[{\"text\":\"%s\"}]}]}", promptText.replace("\"", "\\\"").replace("\n", "\\n"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            System.err.println("Erro na API do Gemini: " + response.statusCode() + " " + response.body());
            return "ERRO_ANALISE_IA: Falha na comunicação com a API.";
        }

        return extrairTextoDaResposta(response.body());
    }

    private String construirPrompt(int idContaOrigem, String chavePixDestino, BigDecimal valor, String historicoConta) {
        return "Analise a seguinte transação PIX para potencial de fraude. " +
               "Responda APENAS com um nível de risco (BAIXO RISCO, MEDIO RISCO, ALTO RISCO) e uma justificativa curta (máximo 15 palavras). " +
               "Seja um analista de fraude financeiro. Considere padrões incomuns.\\n\\n" +
               "--- Dados da Transação ---\\n" +
               "ID da Conta de Origem: " + idContaOrigem + "\\n" +
               "Chave PIX de Destino: " + chavePixDestino + "\\n" +
               "Valor da Transação: R$ " + valor.toPlainString() + "\\n" +
               "Resumo do Histórico da Conta de Origem: " + historicoConta + "\\n\\n" +
               "--- Análise de Risco ---";
    }

    private String extrairTextoDaResposta(String responseBody) throws IOException {
        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            JsonNode textNode = rootNode.at("/candidates/0/content/parts/0/text");
            return textNode.isMissingNode() ? "ERRO_ANALISE_IA: Resposta da IA em formato inválido." : textNode.asText();
        } catch (Exception e) {
            System.err.println("Erro ao parsear JSON da IA: " + responseBody);
            e.printStackTrace();
            return "ERRO_ANALISE_IA: Falha ao ler resposta da IA.";
        }
    }
}
