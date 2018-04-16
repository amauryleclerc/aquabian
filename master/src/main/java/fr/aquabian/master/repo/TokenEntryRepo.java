package fr.aquabian.master.repo;

import org.axonframework.eventhandling.tokenstore.jpa.TokenEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenEntryRepo extends JpaRepository<TokenEntry, TokenEntry.PK> {

}
