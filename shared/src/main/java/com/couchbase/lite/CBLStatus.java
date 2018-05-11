//
// CBLStatus.java
//
// Copyright (c) 2017 Couchbase, Inc All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
package com.couchbase.lite;

import com.couchbase.lite.internal.support.Log;
import com.couchbase.litecore.C4Base;
import com.couchbase.litecore.C4Constants;
import com.couchbase.litecore.C4Error;
import com.couchbase.litecore.LiteCoreException;

class CBLStatus {
    final static String[] kErrorDomains = {
            null,
            CBLError.Domain.CBLErrorDomain,     // LiteCoreDomain
            "POSIXErrorDomain",                 // POSIXDomain
            CBLError.Domain.SQLiteErrorDomain,  // SQLiteDomain
            CBLError.Domain.FleeceErrorDomain,  // FleeceDomain
            CBLError.Domain.CBLErrorDomain,     // Network error
            CBLError.Domain.CBLErrorDomain};    // WebSocketDomain

    static CouchbaseLiteException convertException(int _domain, int _code, LiteCoreException e) {
        String domain = kErrorDomains[_domain];
        int code = _code;
        if (_domain == C4Constants.C4ErrorDomain.NetworkDomain)
            code += CBLError.Code.CBLErrorNetworkBase;
        else if (_domain == C4Constants.C4ErrorDomain.WebSocketDomain)
            code += CBLError.Code.CBLErrorHTTPBase;

        if (domain == null) {
            Log.w(Log.DATABASE, "Unable to map C4Error(%d,%d) to an CouchbaseLiteException", _domain, _code);
            domain = CBLError.Domain.CBLErrorDomain;
            code = CBLError.Code.CBLErrorUnexpectedError;
        }
        if (e != null)
            return new CouchbaseLiteException(e.getMessage(), e, domain, code);
        else
            return new CouchbaseLiteException(domain, code);
    }

    static CouchbaseLiteException convertException(LiteCoreException e) {
        return convertException(e.domain, e.code, e);
    }

    static CouchbaseLiteException convertException(int _domain, int _code, int _internalInfo) {
        if (_domain != 0 && _code != 0) {
            String errMsg = C4Base.getMessage(_domain, _code, _internalInfo);
            return convertException(new LiteCoreException(_domain, _code, errMsg));
        } else
            return convertException(_domain, _code, null);
    }

    static CouchbaseLiteException convertError(C4Error c4err) {
        return convertException(c4err.getDomain(), c4err.getCode(), c4err.getInternalInfo());
    }
}
