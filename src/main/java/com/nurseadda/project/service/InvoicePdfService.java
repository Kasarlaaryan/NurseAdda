package com.nurseadda.project.service;

import com.nurseadda.project.entity.Invoice;

import java.io.ByteArrayOutputStream;

public interface InvoicePdfService {
    ByteArrayOutputStream generateInvoicePdf(Invoice invoice);
}
