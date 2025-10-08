// Mapper: maps between Document entity and DTOs using MapStruct
package com.docutrace.document_service.mapper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.docutrace.document_service.dto.DocumentCreateRequest;
import com.docutrace.document_service.dto.DocumentResponse;
import com.docutrace.document_service.entity.Document;

@Mapper(componentModel = "spring")
public interface DocumentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "processInstanceId", ignore = true)
    Document toEntity(DocumentCreateRequest request);

    @Mapping(target = "statuses", expression = "java(splitStatuses(document.getStatus()))")
    DocumentResponse toResponse(Document document);

    List<DocumentResponse> toResponseList(List<Document> documents);

    default List<String> splitStatuses(String storedStatuses) {
        if (storedStatuses == null || storedStatuses.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(storedStatuses.split("\\|", -1))
                .map(String::trim)
                .toList();
    }
}
