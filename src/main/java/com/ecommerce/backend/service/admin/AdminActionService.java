package com.ecommerce.backend.service.admin;

import com.ecommerce.backend.dto.responses.admin.action.AdminActionResponse;
import com.ecommerce.backend.dto.responses.seller.PageResponse;
import com.ecommerce.backend.entity.AdminAction;
import com.ecommerce.backend.enums.AdminActionType;
import com.ecommerce.backend.enums.EntityType;
import com.ecommerce.backend.repository.AdminActionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminActionService {

    private final AdminActionRepository adminActionRepository;

    public PageResponse<AdminActionResponse> getLogs(
            int page,
            int size,
            EntityType entityType
    ) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<AdminAction> logs = (entityType != null)
                ? adminActionRepository.findByEntityType(entityType, pageable)
                : adminActionRepository.findAll(pageable);

        return new PageResponse<>(
                logs.getContent().stream().map(this::mapToDTO).toList(),
                logs.getNumber(),
                logs.getSize(),
                logs.getTotalElements(),
                logs.getTotalPages()
        );
    }

    public void log(EntityType type, Integer entityId, AdminActionType action, String reason) {
        AdminAction log = new AdminAction();
        log.setEntityType(type);
        log.setEntityId(entityId);
        log.setAction(action);
        log.setReason(reason);

        adminActionRepository.save(log);
    }

    private AdminActionResponse mapToDTO(AdminAction action) {
        return AdminActionResponse.builder()
                .id(action.getId())
                .entityType(action.getEntityType())
                .entityId(action.getEntityId())
                .action(action.getAction())
                .reason(action.getReason())
                .createdAt(action.getCreatedAt())
                .build();
    }
}