package net.gedeon.Collab.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import net.gedeon.Collab.entitie.collaboration.Operation;
import net.gedeon.Collab.entitie.collaboration.OperationType;
import net.gedeon.Collab.entitie.document.Document;

/**
 * OTEngine - Operational Transformation Engine
 * Implements the core OT algorithm for real-time collaborative editing.
 *
 * The OT algorithm ensures consistency when multiple users edit the same
 * document simultaneously
 * by transforming operations based on their context.
 */
@Service
@Slf4j
public class OTEngine {

    /**
     * Transform two concurrent operations against each other.
     * This is the core of the OT algorithm.
     *
     * @param op1 The first operation
     * @param op2 The second operation (to be transformed against op1)
     * @return The transformed version of op2
     */
    public Operation transform(Operation op1, Operation op2) {
        if (op1.getType() == OperationType.INSERT && op2.getType() == OperationType.INSERT) {
            return transformInsertInsert(op1, op2);
        } else if (op1.getType() == OperationType.INSERT && op2.getType() == OperationType.DELETE) {
            return transformInsertDelete(op1, op2);
        } else if (op1.getType() == OperationType.DELETE && op2.getType() == OperationType.INSERT) {
            return transformDeleteInsert(op1, op2);
        } else if (op1.getType() == OperationType.DELETE && op2.getType() == OperationType.DELETE) {
            return transformDeleteDelete(op1, op2);
        }

        return op2;
    }

    /**
     * Transform a client operation against a list of server operations.
     * This is used when a client operation needs to be transformed against
     * multiple operations that happened on the server.
     *
     * @param clientOp  The client operation to transform
     * @param serverOps List of server operations
     * @return The transformed client operation
     */
    public Operation transform(Operation clientOp, List<Operation> serverOps) {
        if (serverOps == null || serverOps.isEmpty()) {
            return clientOp;
        }

        Operation transformedOp = clientOp;
        for (Operation serverOp : serverOps) {
            transformedOp = transform(serverOp, transformedOp);
        }

        return transformedOp;
    }

    /**
     * Apply an operation to a document and return the new content.
     *
     * @param document  The document to modify
     * @param operation The operation to apply
     * @return The new content after applying the operation
     */
    public String apply(Document document, Operation operation) {
        String content = document.getContent() != null ? document.getContent() : "";

        if (operation.getType() == OperationType.INSERT) {
            return applyInsert(content, operation);
        } else if (operation.getType() == OperationType.DELETE) {
            return applyDelete(content, operation);
        }

        return content;
    }

    /**
     * Compose a list of operations into a single operation.
     * This is useful for optimizing operation history.
     *
     * @param operations List of operations to compose
     * @return A single composed operation
     */
    public Operation compose(List<Operation> operations) {
        if (operations == null || operations.isEmpty()) {
            return null;
        }

        if (operations.size() == 1) {
            return operations.get(0);
        }

        // For simplicity, we'll return the last operation
        // In a real implementation, you would merge consecutive operations
        log.info("Composing {} operations", operations.size());
        return operations.get(operations.size() - 1);
    }

    // ==================== Private Helper Methods ====================

    private Operation transformInsertInsert(Operation op1, Operation op2) {
        Operation transformed = copyOperation(op2);

        if (op1.getPosition() < op2.getPosition() ||
                (op1.getPosition() == op2.getPosition() && op1.getUserId().compareTo(op2.getUserId()) < 0)) {
            // op1 comes before op2, shift op2's position
            transformed.setPosition(op2.getPosition() + op1.getLength());
        }

        return transformed;
    }

    private Operation transformInsertDelete(Operation insert, Operation delete) {
        Operation transformed = copyOperation(delete);

        if (insert.getPosition() <= delete.getPosition()) {
            // Insert is before or at delete position, shift delete position
            transformed.setPosition(delete.getPosition() + insert.getLength());
        } else if (insert.getPosition() < delete.getPosition() + delete.getLength()) {
            // Insert is within delete range, increase delete length
            transformed.setLength(delete.getLength() + insert.getLength());
        }

        return transformed;
    }

    private Operation transformDeleteInsert(Operation delete, Operation insert) {
        Operation transformed = copyOperation(insert);

        if (delete.getPosition() + delete.getLength() <= insert.getPosition()) {
            // Delete is completely before insert, shift insert position back
            transformed.setPosition(insert.getPosition() - delete.getLength());
        } else if (delete.getPosition() < insert.getPosition()) {
            // Delete overlaps with insert position, adjust to delete position
            transformed.setPosition(delete.getPosition());
        }

        return transformed;
    }

    private Operation transformDeleteDelete(Operation op1, Operation op2) {
        Operation transformed = copyOperation(op2);

        if (op1.getPosition() + op1.getLength() <= op2.getPosition()) {
            // op1 is completely before op2, shift op2 position back
            transformed.setPosition(op2.getPosition() - op1.getLength());
        } else if (op1.getPosition() >= op2.getPosition() + op2.getLength()) {
            // op1 is completely after op2, no change needed
            return transformed;
        } else {
            // Deletes overlap, adjust accordingly
            int op1End = op1.getPosition() + op1.getLength();
            int op2End = op2.getPosition() + op2.getLength();

            if (op1.getPosition() <= op2.getPosition() && op1End >= op2End) {
                // op1 completely contains op2, op2 becomes empty
                transformed.setLength(0);
            } else if (op1.getPosition() <= op2.getPosition()) {
                // op1 overlaps start of op2
                int overlap = op1End - op2.getPosition();
                transformed.setPosition(op1.getPosition());
                transformed.setLength(op2.getLength() - overlap);
            } else if (op1End >= op2End) {
                // op1 overlaps end of op2
                transformed.setLength(op1.getPosition() - op2.getPosition());
            } else {
                // op1 is inside op2
                transformed.setLength(op2.getLength() - op1.getLength());
            }
        }

        return transformed;
    }

    private String applyInsert(String content, Operation operation) {
        int pos = operation.getPosition();
        if (pos < 0 || pos > content.length()) {
            log.warn("Invalid insert position: {}, content length: {}", pos, content.length());
            return content;
        }

        String insertContent = operation.getContent() != null ? operation.getContent() : "";
        return content.substring(0, pos) + insertContent + content.substring(pos);
    }

    private String applyDelete(String content, Operation operation) {
        int pos = operation.getPosition();
        int length = operation.getLength();

        if (pos < 0 || pos > content.length() || pos + length > content.length()) {
            log.warn("Invalid delete operation: pos={}, length={}, content length={}",
                    pos, length, content.length());
            return content;
        }

        return content.substring(0, pos) + content.substring(pos + length);
    }

    private Operation copyOperation(Operation original) {
        return Operation.builder()
                .id(original.getId())
                .document(original.getDocument())
                .userId(original.getUserId())
                .type(original.getType())
                .position(original.getPosition())
                .content(original.getContent())
                .length(original.getLength())
                .clientVersion(original.getClientVersion())
                .serverVersion(original.getServerVersion())
                .appliedAt(original.getAppliedAt())
                .build();
    }
}
