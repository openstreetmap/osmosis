// This software is released into the Public Domain.  See copying.txt for details.

package org.openstreetmap.osmosis.tagtransform.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.QuoteMode;
import org.w3c.dom.NamedNodeMap;

import org.openstreetmap.osmosis.tagtransform.DataSource;

/**
 *
 * @author fwiesweg
 */
public class DataSourceCSV implements DataSource {
    
        private Map<String, String[]> data = Collections.emptyMap();
        private String[] fallback;
    
        public DataSourceCSV(File parentDirectory, NamedNodeMap attributes) {
            String file = attributes.getNamedItem("file").getTextContent();
            String csvFormat = attributes.getNamedItem("csvFormat").getTextContent();
            
            
            List<Integer> lookupIndices = Stream.of(attributes.getNamedItem("lookup").getTextContent().split(","))
                    .map(idx -> Integer.parseInt(idx))
                    .collect(Collectors.toList());
            List<Integer> returnIndices = Stream.of(attributes.getNamedItem("return").getTextContent().split(","))
                    .map(idx -> Integer.parseInt(idx))
                    .collect(Collectors.toList());
            
            try(InputStreamReader fis = new InputStreamReader(new FileInputStream(new File(parentDirectory, file)))) {
                CSVFormat format = CSVFormat.Builder.create().setDelimiter(',').setQuote('"').setQuoteMode(QuoteMode.MINIMAL).build();
                CSVRecord fallbackRecord = CSVParser.parse(attributes.getNamedItem("fallback").getTextContent(), format)
                        .iterator().next();
                this.fallback = new String[fallbackRecord.size()];
                for(int i = 0; i < fallback.length; i++) {
                    this.fallback[i] = fallbackRecord.get(i);
                }
                
                CSVParser parser = CSVParser.parse(fis, CSVFormat.valueOf(csvFormat));
                this.data = parser.getRecords().stream().collect(Collectors.toMap(
                                r -> {
                                        return lookupIndices.stream().map(i -> r.get(i)).collect(Collectors.joining("\0"));
                                },
                                r -> {
                                        return returnIndices.stream().map(i -> r.get(i)).toArray(i -> new String[i]);
                                }));
                        
                
            } catch (IOException ex) {
                Logger.getLogger(DataSourceCSV.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        @Override
        public String[] transform(String[] matches) {
            return data.getOrDefault(String.join("\0", matches), fallback);
        }
}
