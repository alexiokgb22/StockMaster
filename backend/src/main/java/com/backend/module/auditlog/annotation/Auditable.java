package com.backend.module.auditlog.annotation;

import java.lang.annotation.*;

/**
 * Marque une méthode de service pour traçabilité automatique.
 * L'aspect {@link com.backend.module.auditlog.aspect.AuditAspect} intercepte
 * les appels et enregistre l'action dans le journal des audits.
 *
 * <pre>
 * &#64;Auditable(module = "reception", action = "VALIDATE", entity = "Reception")
 * public ReceptionResponse validate(Long warehouseId, Long receptionId) { ... }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Auditable {

    /** Module métier (ex: "reception", "stock", "transfer", "dispatch"). */
    String module();

    /** Action réalisée (ex: "CREATE", "VALIDATE", "REJECT", "DELETE", "UPDATE"). */
    String action();

    /** Nom de l'entité principale (ex: "Reception", "Stock", "PurchaseOrder"). */
    String entity();

    /** Description lisible de l'action (supporte le placeholder {result} pour l'id retourné). */
    String description() default "";
}
