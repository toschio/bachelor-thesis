/*
    Copyright 2013 Rustici Software

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
package com.edutec.serving.models.xapimodels;

import com.edutec.serving.models.xapimodels.json.Mapper;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

/**
 * Language map
 */
@NoArgsConstructor
@Data
public class LanguageMap implements Iterable<Entry<String, String>> {

    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    private final HashMap<String, String> _map = new HashMap<String, String>();

    public LanguageMap(JsonNode jsonNode) {
        this();

        Iterator<Entry<String, JsonNode>> items = jsonNode.fields();
        while (items.hasNext()) {
            Entry<String, JsonNode> item = items.next();

            this.put(item.getKey(), item.getValue().textValue());
        }
    }

    public ObjectNode toJSONNode(TCAPIVersion version) {
        ObjectNode node = Mapper.getInstance().createObjectNode();

        for (Entry<String, String> entry : this._map.entrySet()) {
            node.put(entry.getKey(), entry.getValue());
        }

        return node;
    }

    public String put(String key, String val) {
        return this._map.put(key, val);
    }

    public String put(Entry<String, String> entry) {
        return this.put(entry.getKey(), entry.getValue());
    }

    public String get(String key) {
        return this._map.get(key);
    }

    public boolean containsKey(String key) {
        return this._map.containsKey(key);
    }

    public boolean containsValue(String value) {
        return this._map.containsValue(value);
    }

    public Entry<String, String> findFirstValue(String value) {
        Entry<String, String> retVal = null;
        Iterator<Entry<String, String>> it = this.iterator();
        while (it.hasNext()) {
            Entry<String, String> n = it.next();
            if (n.getValue().equalsIgnoreCase(value)) {
                retVal = n;
                break;
            }
        }
        return retVal;
    }

    public void populateWithAvailableLanguages(List<String> list) {
        Iterator<Entry<String, String>> it = this.iterator();
        while (it.hasNext()) {
            Entry<String, String> n = it.next();
            list.add(n.getKey());
        }
    }

    @Override
    public Iterator<Entry<String, String>> iterator() {
        return new LanguageMapIterator();
    }

    private class LanguageMapIterator implements Iterator<Entry<String, String>> {
        private Iterator<Entry<String, String>> iterator;

        public LanguageMapIterator() {
            iterator = _map.entrySet().iterator();
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Entry<String, String> next() {
            return iterator.next();
        }

        @Override
        public void remove() throws UnsupportedOperationException {
            throw new UnsupportedOperationException(
                    "LanguageMap iterator does not implement the remove method"
            );
        }
    }
}
