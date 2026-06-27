package com.backend.module.dispatch.service;

import com.backend.module.dispatch.entity.Dispatch;
import com.backend.module.dispatch.entity.DispatchLine;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class DispatchBordereauService {

    public String generateHtml(Dispatch dispatch) {
        StringBuilder html = new StringBuilder();
        List<DispatchLine> lines = dispatch.getLines() != null
                ? dispatch.getLines().stream().toList()
                : List.of();

        html.append("<!DOCTYPE html>")
                .append("<html lang='fr'>")
                .append("<head><meta charset='UTF-8'><title>Bordereau de sortie</title>")
                .append("<style>")
                .append("body{font-family:Arial,sans-serif;margin:24px;color:#111827;} ")
                .append("h1{margin-bottom:8px;} ")
                .append(".card{border:1px solid #d1d5db;border-radius:10px;padding:16px;margin-bottom:16px;} ")
                .append("table{width:100%;border-collapse:collapse;margin-top:12px;} ")
                .append("th,td{border:1px solid #e5e7eb;padding:8px;text-align:left;} ")
                .append("th{background:#f3f4f6;} ")
                .append(".label{font-weight:bold;color:#374151;} ")
                .append("</style></head><body>")
                .append("<h1>Bordereau de sortie</h1>")
                .append("<p><strong>Numéro :</strong> ").append(escapeHtml(dispatch.getDispatchNumber())).append("</p>")
                .append("<div class='card'>")
                .append("<p><span class='label'>Entrepôt :</span> ").append(escapeHtml(dispatch.getWarehouse() != null ? dispatch.getWarehouse().getName() : "")).append("</p>")
                .append("<p><span class='label'>Créé le :</span> ")
                .append(dispatch.getCreatedAt() != null
                        ? dispatch.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                        : "")
                .append("</p>")
                .append("<p><span class='label'>Statut :</span> ").append(dispatch.getStatus()).append("</p>")
                .append("</div>")
                .append("<div class='card'>")
                .append("<h2>Informations client</h2>")
                .append("<p><span class='label'>Nom prénom :</span> ")
                .append(escapeHtml(formatClientName(dispatch)))
                .append("</p>")
                .append("<p><span class='label'>Téléphone :</span> ")
                .append(escapeHtml(dispatch.getClientPhone()))
                .append("</p>")
                .append("<p><span class='label'>Adresse de livraison :</span> ")
                .append(escapeHtml(dispatch.getDeliveryAddress()))
                .append("</p>")
                .append("</div>")
                .append("<div class='card'>")
                .append("<h2>Produits sortis</h2>")
                .append("<table>")
                .append("<tr><th>Produit</th><th>Référence</th><th>Zone</th><th>Quantité</th><th>Observation</th></tr>");

        for (DispatchLine line : lines) {
            html.append("<tr>")
                    .append("<td>").append(escapeHtml(line.getProduct() != null ? line.getProduct().getName() : "")).append("</td>")
                    .append("<td>").append(escapeHtml(line.getProduct() != null ? line.getProduct().getReference() : "")).append("</td>")
                    .append("<td>").append(escapeHtml(line.getZone() != null ? line.getZone().getName() : "")).append("</td>")
                    .append("<td>").append(line.getQuantityRequested()).append("</td>")
                    .append("<td>").append(escapeHtml(line.getNote())).append("</td>")
                    .append("</tr>");
        }

        html.append("</table>")
                .append("</div>")
                .append("<div class='card'>")
                .append("<p><span class='label'>Note :</span> ")
                .append(escapeHtml(dispatch.getNote()))
                .append("</p>")
                .append("</div>")
                .append("</body></html>");

        return html.toString();
    }

    private String formatClientName(Dispatch dispatch) {
        String firstName = dispatch.getClientFirstName() != null ? dispatch.getClientFirstName().trim() : "";
        String lastName = dispatch.getClientLastName() != null ? dispatch.getClientLastName().trim() : "";
        return (firstName + " " + lastName).trim();
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
