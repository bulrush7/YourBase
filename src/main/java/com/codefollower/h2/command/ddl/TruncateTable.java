/*
 * Copyright 2004-2011 H2 Group. Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package com.codefollower.h2.command.ddl;

import com.codefollower.h2.command.CommandInterface;
import com.codefollower.h2.constant.ErrorCode;
import com.codefollower.h2.engine.Right;
import com.codefollower.h2.engine.Session;
import com.codefollower.h2.message.DbException;
import com.codefollower.h2.table.Table;

/**
 * This class represents the statement
 * TRUNCATE TABLE
 */
public class TruncateTable extends DefineCommand {

    private Table table;

    public TruncateTable(Session session) {
        super(session);
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public int update() {
        session.commit(true);
        if (!table.canTruncate()) {
            throw DbException.get(ErrorCode.CANNOT_TRUNCATE_1, table.getSQL());
        }
        session.getUser().checkRight(table, Right.DELETE);
        table.lock(session, true, true);
        table.truncate(session);
        return 0;
    }

    public int getType() {
        return CommandInterface.TRUNCATE_TABLE;
    }

}
