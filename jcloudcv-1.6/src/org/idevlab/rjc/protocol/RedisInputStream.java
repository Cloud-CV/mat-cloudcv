/*
 * Copyright 2010-2011. Evgeny Dolgov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.idevlab.rjc.protocol;

import org.idevlab.rjc.RedisException;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class RedisInputStream extends FilterInputStream {

    protected final byte buf[];

    protected int count, limit;

    public RedisInputStream(InputStream in, int size) {
        super(in);
        if (size <= 0) {
            throw new IllegalArgumentException("Buffer size <= 0");
        }
        buf = new byte[size];
    }

    public RedisInputStream(InputStream in) {
        this(in, 8192);
    }

    public byte readByte() throws IOException {
        if (count == limit) {
            fill();
        }

        return buf[count++];
    }

    public String readLine() {
        int b;
        byte c;
        StringBuilder sb = new StringBuilder();

        try {
            while (true) {
                if (count == limit) {
                    fill();
                }
                if (limit == -1)
                    break;

                b = buf[count++];
                if (b == '\r') {
                    if (count == limit) {
                        fill();
                    }

                    if (limit == -1) {
                        sb.append((char) b);
                        break;
                    }

                    c = buf[count++];
                    if (c == '\n') {
                        break;
                    }
                    sb.append((char) b);
                    sb.append((char) c);
                } else {
                    sb.append((char) b);
                }
            }
        } catch (IOException e) {
            throw new RedisException(e);
        }
        String reply = sb.toString();
        if (reply.isEmpty()) {
            throw new RedisException(
                    "It seems like server has closed the connection.");
        }
        return reply;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (count == limit) {
            fill();
            if (limit == -1)
                return -1;
        }
        final int length = Math.min(limit - count, len);
        System.arraycopy(buf, count, b, off, length);
        count += length;
        return length;
    }

    private void fill() throws IOException {
        limit = in.read(buf);
        count = 0;
    }
}
