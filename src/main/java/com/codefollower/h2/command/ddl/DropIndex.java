/*
 * Copyright 2004-2011 H2 Group. Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package com.codefollower.h2.command.ddl;

import java.util.ArrayList;

import com.codefollower.h2.command.CommandInterface;
import com.codefollower.h2.constant.ErrorCode;
import com.codefollower.h2.constraint.Constraint;
import com.codefollower.h2.engine.Database;
import com.codefollower.h2.engine.Right;
import com.codefollower.h2.engine.Session;
import com.codefollower.h2.index.Index;
import com.codefollower.h2.message.DbException;
import com.codefollower.h2.schema.Schema;
import com.codefollower.h2.table.Table;

/**
 * This class represents the statement
 * DROP INDEX
 */
public class DropIndex extends SchemaCommand {

    private String indexName;
    private boolean ifExists;

    public DropIndex(Session session, Schema schema) {
        super(session, schema);
    }

    public void setIfExists(boolean b) {
        ifExists = b;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public int update() {
        session.commit(true);
        Database db = session.getDatabase();
        Index index = getSchema().findIndex(session, indexName);
        if (index == null) {
            if (!ifExists) {
                throw DbException.get(ErrorCode.INDEX_NOT_FOUND_1, indexName);
            }
        } else {
            Table table = index.getTable();
            session.getUser().checkRight(index.getTable(), Right.ALL);
            Constraint pkConstraint = null;
            ArrayList<Constraint> constraints = table.getConstraints();
            for (int i = 0; constraints != null && i < constraints.size(); i++) {
                Constraint cons = constraints.get(i);
                if (cons.usesIndex(index)) {
                    // can drop primary key index (for compatibility)
                    if (Constraint.PRIMARY_KEY.equals(cons.getConstraintType())) {
                        pkConstraint = cons;
                    } else {
                        throw DbException.get(ErrorCode.INDEX_BELONGS_TO_CONSTRAINT_1, indexName);
                    }
                }
            }
            index.getTable().setModified();
            if (pkConstraint != null) {
                db.removeSchemaObject(session, pkConstraint);
            } else {
                db.removeSchemaObject(session, index);
            }
        }
        return 0;
    }

    public int getType() {
        return CommandInterface.DROP_INDEX;
    }

}
