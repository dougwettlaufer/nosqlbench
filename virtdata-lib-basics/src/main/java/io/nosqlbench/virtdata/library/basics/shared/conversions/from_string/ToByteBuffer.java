/*
 *
 *       Copyright 2015 Jonathan Shook
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package io.nosqlbench.virtdata.library.basics.shared.conversions.from_string;

import io.nosqlbench.virtdata.annotations.Categories;
import io.nosqlbench.virtdata.annotations.Category;
import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

@ThreadSafeMapper
@Categories({Category.conversion})
public class ToByteBuffer implements Function<String,ByteBuffer> {

    @Override
    public ByteBuffer apply(String input) {
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
        return ByteBuffer.wrap(bytes);
    }
}