// Copyright Keith D Gregory
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.kdgregory.kdgcommons.codec;


/**
 *  This exception is thrown during decoding, when an invalid character is found in
 *  the source stream.
 *
 *  @since 1.0.14
 */
public class InvalidSourceByteException
extends CodecException
{
    private static final long serialVersionUID = 1L;

    private byte sourceByte;


    public InvalidSourceByteException(int c)
    {
        super("invalid source byte: " + c);
        sourceByte = (byte)c;
    }


    /**
     *  Returns the invalid byte.
     */
    public byte getInvalidByte()
    {
        return sourceByte;
    }
}
