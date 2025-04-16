package com.hhu.controller;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentByLineSplitter;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class KnowledgeController {

    final EmbeddingStore<TextSegment> embeddingStore;

    final EmbeddingModel embeddingModel;
    @GetMapping("/load")
    public String load(){
        ///TODO这里改成自己的知识库路径
        List<Document> documents = FileSystemDocumentLoader
                .loadDocuments("QAQAQAQAQAQAQAQAQAQAQ");
        for (Document document : documents) {
            System.out.println(document.text());
        }
        // EmbeddingStoreIngestor.ingest(documents,embeddingStore);
        EmbeddingStoreIngestor.builder().embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .documentSplitter(new DocumentByParagraphSplitter(100,60))
//                .documentSplitter(new DocumentByLineSplitter(100,60))
                .build().ingest(documents);

        String back = "成功处理";
        return back;
    }
}
