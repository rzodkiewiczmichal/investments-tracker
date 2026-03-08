package com.investments.tracker.application.usecase;

import java.io.InputStream;

import com.investments.tracker.domain.model.ImportSession;
import com.investments.tracker.domain.model.value.AccountName;

/**
 * Use case for initiating a broker import session.
 *
 * <p>Parses the uploaded file, attempts exact symbol matching against the instrument catalog, and
 * creates an import session. Unmatched instruments are returned to the user for manual mapping.
 */
public interface InitiateImportUseCase {

    /**
     * Initiates an import by parsing a broker file and creating an import session.
     *
     * @param brokerName the broker identifier (e.g., "mBank")
     * @param accountName the target account name
     * @param file the broker export file input stream
     * @return the created import session (status PENDING_REVIEW or READY_TO_CONFIRM)
     */
    ImportSession initiateImport(String brokerName, AccountName accountName, InputStream file);
}
