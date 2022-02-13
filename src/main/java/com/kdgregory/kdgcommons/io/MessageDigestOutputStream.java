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

package com.kdgregory.kdgcommons.io;

import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.kdgregory.kdgcommons.codec.HexCodec;


/**
 *  An alternative approach to computing a message digest. This is simpler to
 *  write when simultaneously writing to a stream, as it uses the same methods.
 *
 *  @since 1.0.18
 */
public class MessageDigestOutputStream
extends OutputStream
{
    private MessageDigest digester;
    private byte[] digest;


    /**
     *  Constructs a new instance. Translates the checked NoSuchAlgorithmException
     *  into a RuntimeException (it's likely to be a coding error, and catching it
     *  is rarely helpful).
     */
    public MessageDigestOutputStream(String algorithm)
    {
        try
        {
            digester = MessageDigest.getInstance(algorithm);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException("Unsupported algorithm: " + algorithm, e);
        }
    }

//----------------------------------------------------------------------------
//  OutputStream
//----------------------------------------------------------------------------

    @Override
    public void write(int b) throws IOException
    {
        if (digest != null)
            throw new IllegalStateException("stream has already been digested");

        digester.update((byte)b);
    }


    @Override
    public void write(byte[] b) throws IOException
    {
        if (digest != null)
            throw new IllegalStateException("stream has already been digested");

        digester.update(b);
    }


    @Override
    public void write(byte[] b, int off, int len) throws IOException
    {
        if (digest != null)
            throw new IllegalStateException("stream has already been digested");

        digester.update(b, off, len);
    }


    /**
     *  Closes the stream. After this point no more bytes may be written.
     */
    @Override
    public void close() throws IOException
    {
        digest();
    }

//----------------------------------------------------------------------------
//  Other public methods
//----------------------------------------------------------------------------

    /**
     *  Returns the stream digest. After calling this method, no more bytes may
     *  be written to the stream. This method may be called multiple times.
     */
    public byte[] digest()
    {
        if (digest == null)
        {
            digest = digester.digest();
        }
        return digest;
    }


    /**
     *  Returns a hex-encoded (lowercase) version of the stream digest. After
     *  calling this method, no more bytes may be written to the stream. This
     *  method may be called multiple times.
     */
    public String digestAsString()
    {
        digest();
        return new HexCodec().toString(digest).toLowerCase();
    }
}
