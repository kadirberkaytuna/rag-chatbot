package com.berkay.rag_chatbot;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface Assistant {

    @SystemMessage("""
            Sen yüklenen dökümanlara dayanarak soruları yanıtlayan bir asistansın.
            Cevabı yalnızca sağlanan döküman içeriğine dayandır.
            Eğer cevap dökümanlarda yoksa, bilmediğini açıkça söyle.
            """)
    String chat(String userMessage);
}