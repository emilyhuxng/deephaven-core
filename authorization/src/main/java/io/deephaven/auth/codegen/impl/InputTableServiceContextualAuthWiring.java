//
// Copyright (c) 2016-2022 Deephaven Data Labs and Patent Pending
//
// ---------------------------------------------------------------------
// This class is generated by GenerateContextualAuthWiring. DO NOT EDIT!
// ---------------------------------------------------------------------
//
package io.deephaven.auth.codegen.impl;

import io.deephaven.auth.AuthContext;
import io.deephaven.auth.ServiceAuthWiring;
import io.deephaven.engine.table.Table;
import io.deephaven.proto.backplane.grpc.AddTableRequest;
import io.deephaven.proto.backplane.grpc.DeleteTableRequest;
import java.util.List;

/**
 * This interface provides type-safe authorization hooks for InputTableServiceGrpc.
 */
public interface InputTableServiceContextualAuthWiring {
    /**
     * Authorize a request to AddTableToInputTable.
     *
     * @param authContext the authentication context of the request
     * @param request the request to authorize
     * @param sourceTables the operation's source tables
     * @throws io.grpc.StatusRuntimeException if the user is not authorized to invoke AddTableToInputTable
     */
    void checkPermissionAddTableToInputTable(AuthContext authContext, AddTableRequest request,
            List<Table> sourceTables);

    /**
     * Authorize a request to DeleteTableFromInputTable.
     *
     * @param authContext the authentication context of the request
     * @param request the request to authorize
     * @param sourceTables the operation's source tables
     * @throws io.grpc.StatusRuntimeException if the user is not authorized to invoke DeleteTableFromInputTable
     */
    void checkPermissionDeleteTableFromInputTable(AuthContext authContext, DeleteTableRequest request,
            List<Table> sourceTables);

    class AllowAll implements InputTableServiceContextualAuthWiring {
        public void checkPermissionAddTableToInputTable(AuthContext authContext,
                AddTableRequest request, List<Table> sourceTables) {}

        public void checkPermissionDeleteTableFromInputTable(AuthContext authContext,
                DeleteTableRequest request, List<Table> sourceTables) {}
    }

    class DenyAll implements InputTableServiceContextualAuthWiring {
        public void checkPermissionAddTableToInputTable(AuthContext authContext,
                AddTableRequest request, List<Table> sourceTables) {
            ServiceAuthWiring.operationNotAllowed();
        }

        public void checkPermissionDeleteTableFromInputTable(AuthContext authContext,
                DeleteTableRequest request, List<Table> sourceTables) {
            ServiceAuthWiring.operationNotAllowed();
        }
    }

    class TestUseOnly implements InputTableServiceContextualAuthWiring {
        public InputTableServiceContextualAuthWiring delegate;

        public void checkPermissionAddTableToInputTable(AuthContext authContext,
                AddTableRequest request, List<Table> sourceTables) {
            if (delegate != null) {
                delegate.checkPermissionAddTableToInputTable(authContext, request, sourceTables);
            }
        }

        public void checkPermissionDeleteTableFromInputTable(AuthContext authContext,
                DeleteTableRequest request, List<Table> sourceTables) {
            if (delegate != null) {
                delegate.checkPermissionDeleteTableFromInputTable(authContext, request, sourceTables);
            }
        }
    }
}
