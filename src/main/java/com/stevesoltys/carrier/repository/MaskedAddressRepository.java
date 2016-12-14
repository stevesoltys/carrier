package com.stevesoltys.carrier.repository;

import com.stevesoltys.carrier.model.MaskedAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * @author Steve Soltys
 */
@Repository
@Transactional
public interface MaskedAddressRepository extends JpaRepository<MaskedAddress, Long> {

    Optional<MaskedAddress> findByAddress(String address);

    @Query("select a from MaskedAddress a join a.replyAddresses r where ?1 in (KEY(r))")
    Optional<MaskedAddress> findByReplyAddress(String address);

}
