package com.stevesoltys.carrier.repository;

import com.stevesoltys.carrier.model.MaskedAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * The {@link MaskedAddress} repository.
 *
 * @author Steve Soltys
 */
@Repository
@Transactional
public interface MaskedAddressRepository extends JpaRepository<MaskedAddress, Long> {

    /**
     * Finds a masked address.
     *
     * @param address The e-mail address for the masked address.
     * @return An optional, possibly containing the masked address.
     */
    Optional<MaskedAddress> findByAddress(String address);

    /**
     * Finds a masked address, given the generated reply address.
     *
     * @param address The generated reply address.
     * @return An optional, possibly containing the masked address.
     */
    @Query("select a from MaskedAddress a join a.replyAddresses r where ?1 in (KEY(r))")
    Optional<MaskedAddress> findByReplyAddress(String address);

}
