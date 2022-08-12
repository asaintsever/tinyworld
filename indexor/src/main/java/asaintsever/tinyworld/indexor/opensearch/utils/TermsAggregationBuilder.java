/*
 * Copyright 2021-2022 A. Saint-Sever
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 * More information about this project is available at:
 *
 *    https://github.com/asaintsever/tinyworld
 */
package asaintsever.tinyworld.indexor.opensearch.utils;

import java.util.ArrayList;
import java.util.List;

import org.opensearch.search.aggregations.Aggregation;
import org.opensearch.search.aggregations.Aggregations;
import org.opensearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.opensearch.search.aggregations.bucket.terms.Terms;

import asaintsever.tinyworld.indexor.search.results.TermsAggregation;
import asaintsever.tinyworld.indexor.search.results.TermsAggregation.Bucket;

public class TermsAggregationBuilder {

    public static List<TermsAggregation> from(Aggregations aggregations) {
        List<TermsAggregation> buckAggrList = new ArrayList<TermsAggregation>();

        if (aggregations != null) {
            List<Aggregation> aggrList = aggregations.asList();
            if (aggrList != null && aggrList.size() > 0) {
                for (Aggregation aggregation : aggrList) {
                    // We only support Terms aggregation
                    // (https://www.elastic.co/guide/en/elasticsearch/reference/current/search-aggregations-bucket-terms-aggregation.html)
                    if (ParsedStringTerms.class.isInstance(aggregation)) {
                        TermsAggregation buckAggr = new TermsAggregation();

                        ParsedStringTerms aggr = (ParsedStringTerms) aggregation;
                        buckAggr.setName(aggr.getName());
                        buckAggr.setSum_other_doc_count(aggr.getSumOfOtherDocCounts());

                        List<? extends Terms.Bucket> buckets = aggr.getBuckets();
                        if (buckets != null && buckets.size() > 0) {
                            for (Terms.Bucket bucket : buckets) {
                                Bucket buck = buckAggr.new Bucket();
                                buck.setKey(bucket.getKeyAsString());
                                buck.setSubAggregations(from(bucket.getAggregations()));
                                buckAggr.getBuckets().add(buck);
                            }
                        }

                        buckAggrList.add(buckAggr);
                    }
                }
            }
        }

        return buckAggrList;
    }
}
