package com.stevesoltys.carrier.service;

import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;
import org.springframework.stereotype.Service;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The mail resolver service. Used to resolve a SMTP server address by taking the domain in an e-mail address,
 * doing a DNS lookup, and parsing the MX records.
 *
 * @author Steve Soltys
 */
@Service
public class MailResolverService {

    /**
     * The pattern used for matching the domain in an e-mail address.
     */
    private static final Pattern DOMAIN_MATCH_PATTERN = Pattern.compile("([^@]*)@(.*)");

    /**
     * Attempts to resolve an SMTP server address, given the destination e-mail address.
     *
     * @param email The e-mail address used for the lookup.
     * @return An optional, potentially containing
     */
    Optional<String> resolve(String email) {
        Optional<String> domainOptional = extractDomain(email);

        if (!domainOptional.isPresent()) {
            return Optional.empty();
        }

        String mailDomain = domainOptional.get();

        try {
            Lookup dnsLookup = new Lookup(mailDomain, Type.MX);
            List<Record> records = Arrays.asList(dnsLookup.run());

            if (records.isEmpty()) {
                return Optional.empty();
            }

            return chooseBestRecord(records);

        } catch (TextParseException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    /**
     * Chooses the best SMTP server, given a list of MX records.
     * TODO: Actually choose the best record!
     *
     * @param records The MX records.
     * @return An optional, possibly containing an SMTP server address.
     */
    private Optional<String> chooseBestRecord(List<Record> records) {
        TreeMultimap<Integer, String> recordMap = decodeRecords(records);

        if(!recordMap.isEmpty()) {
            List<String> topRecords = new LinkedList<>(recordMap.asMap().firstEntry().getValue());

            if(!topRecords.isEmpty()) {
                String record = topRecords.get(0);

                return Optional.of(record.substring(0, record.length() - 1));
            }

        }

        return Optional.empty();
    }

    /**
     * Decodes a list of MX records into a tree map, ranking them automatically.
     *
     * @param records The list of MX records.
     * @return The tree map containing ranked MX records.
     */
    private TreeMultimap<Integer, String> decodeRecords(List<Record> records) {
        TreeMultimap<Integer, String> recordMap = TreeMultimap.create(Ordering.natural(), Ordering.natural());

        records.forEach(record -> {
            String[] split = record.rdataToString().split(" ");

            if (split.length >= 2) {

                try {
                    int rank = Integer.parseInt(split[0]);
                    String domain = split[1];

                    recordMap.put(rank, domain);

                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                }

            }
        });

        return recordMap;
    }

    /**
     * Extracts the domain from the given e-mail address.
     *
     * @param email The e-mail address.
     * @return An optional, possibly contianing the e-mail address domain.
     */
    private Optional<String> extractDomain(String email) {
        Matcher matcher = DOMAIN_MATCH_PATTERN.matcher(email);

        if (matcher.find()) {
            String domain = matcher.group(2);

            return Optional.of(domain);
        }

        return Optional.empty();
    }
}
