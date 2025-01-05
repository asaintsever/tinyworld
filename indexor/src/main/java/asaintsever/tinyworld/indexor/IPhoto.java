/*
 * Copyright 2021-2025 A. Saint-Sever
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
package asaintsever.tinyworld.indexor;

import java.io.IOException;
import java.util.List;

import asaintsever.tinyworld.indexor.search.results.IndexPage;
import asaintsever.tinyworld.indexor.search.results.TermsAggregation;
import asaintsever.tinyworld.metadata.extractor.PhotoMetadata;

public interface IPhoto {

    String add(PhotoMetadata photo, boolean allowUpdate) throws IOException;

    PhotoMetadata get(String id) throws IOException;

    long count() throws IOException;

    List<TermsAggregation> getAggregations(String searchTemplateId) throws IOException;

    IndexPage<PhotoMetadata> search(String query, int from, int size) throws IOException;

    IndexPage<PhotoMetadata> next(IndexPage<PhotoMetadata> page) throws IOException;
}
