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

package com.kdgregory.kdgcommons.bean;


/**
 *  This exception exists to transform the checked exceptions thrown by
 *  reflection classes into a runtime exception.
 *
 *  @since 1.0.5
 */
public class IntrospectionException
extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public IntrospectionException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
