/*
 * Copyright 2011 The Apache Software Foundation
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.codefollower.yourbase.result;

import java.util.List;
import java.util.Properties;

import org.apache.hadoop.hbase.util.Bytes;

import com.codefollower.h2.command.dml.Query;
import com.codefollower.h2.engine.Session;
import com.codefollower.h2.result.LocalResult;
import com.codefollower.h2.result.ResultInterface;
import com.codefollower.h2.table.TableFilter;
import com.codefollower.h2.util.ValueHashMap;
import com.codefollower.h2.value.Value;
import com.codefollower.h2.value.ValueArray;
import com.codefollower.yourbase.command.CommandProxy;
import com.codefollower.yourbase.command.RowKeyConditionInfo;
import com.codefollower.yourbase.util.HBaseRegionInfo;
import com.codefollower.yourbase.util.HBaseUtils;

public class CombinedResult implements ResultInterface {

    private final Session session;
    private final Query query;
    private final int maxrows;
    private final byte[] tableName;
    private ResultInterface result;
    private List<byte[]> startKeys;
    private String[] sqls;
    private int index;
    private int size;
    private ValueHashMap<Value[]> distinctRows;

    public CombinedResult(Session session, Query query, int maxrows) {
        this.session = session;
        this.query = query;
        this.maxrows = maxrows;
        this.tableName = HBaseUtils.toBytes(query.getTableName());
        if (!query.isDistributed()) {
            result = query.query(maxrows);
        } else {
            RowKeyConditionInfo rkci = query.getRowKeyConditionInfo();
            startKeys = rkci.getStartKeys();
            sqls = rkci.getPlanSQLs();
            size = sqls.length;
            nextResult();
        }
    }

    public CombinedResult(TableFilter filter) {
        this.session = filter.getSession();
        this.query = filter.getSelect();
        this.maxrows = -1;
        this.tableName = HBaseUtils.toBytes(filter.getTable().getName());

        RowKeyConditionInfo rkci = filter.getSelect().getRowKeyConditionInfo(filter);
        startKeys = rkci.getStartKeys();
        sqls = rkci.getPlanSQLs(filter);
        size = sqls.length;
        nextResult();
    }

    private void closeAllSilently() {
        if (result != null)
            result.close();
    }

    private boolean nextResult() {
        if (index >= size)
            return false;

        closeAllSilently();

        try {
            HBaseRegionInfo hri = HBaseUtils.getHBaseRegionInfo(tableName, startKeys.get(index));

            if (CommandProxy.isLocal(session, hri)) {
                session.setRegionName(Bytes.toBytes(hri.getRegionName()));
                result = session.prepareLocal(sqls[index]).query(maxrows);
            } else {
                Properties info = new Properties(session.getOriginalProperties());
                info.setProperty("REGION_NAME", hri.getRegionName());
                result = CommandProxy.getCommandInterface(session, info, hri.getRegionServerURL(), sqls[index],
                        query.getParameters()).executeQuery(maxrows, false);
            }
            index++;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public boolean containsDistinct(Value[] values) {
        if (result instanceof LocalResult)
            return ((LocalResult) result).containsDistinct(values);

        if (distinctRows == null) {
            distinctRows = ValueHashMap.newInstance();
            int visibleColumnCount = getVisibleColumnCount();
            while (next()) {
                Value[] row = currentRow();
                if (row.length > visibleColumnCount) {
                    Value[] r2 = new Value[visibleColumnCount];
                    System.arraycopy(row, 0, r2, 0, visibleColumnCount);
                    row = r2;
                }
                ValueArray array = ValueArray.get(row);
                distinctRows.put(array, row);
            }
        }

        ValueArray array = ValueArray.get(values);
        return distinctRows.get(array) != null;
    }

    public void reset() {
        result.reset();
    }

    public Value[] currentRow() {
        return result.currentRow();
    }

    public boolean next() {
        boolean next = result.next();
        if (!next) {
            next = nextResult();
            if (next)
                next = result.next();
        }
        return next;
    }

    public int getRowId() {
        return result.getRowId();
    }

    public int getVisibleColumnCount() {
        return result.getVisibleColumnCount();
    }

    public int getRowCount() {
        int count = result.getRowCount();
        while (count == 0 && nextResult())
            count = result.getRowCount();
        return count;
    }

    public boolean needToClose() {
        return result.needToClose();
    }

    public void close() {
        closeAllSilently();
    }

    public String getAlias(int i) {
        return result.getAlias(i);
    }

    public String getSchemaName(int i) {
        return result.getSchemaName(i);
    }

    public String getTableName(int i) {
        return result.getTableName(i);
    }

    public String getColumnName(int i) {
        return result.getColumnName(i);
    }

    public int getColumnType(int i) {
        return result.getColumnType(i);
    }

    public long getColumnPrecision(int i) {
        return result.getColumnPrecision(i);
    }

    public int getColumnScale(int i) {
        return result.getColumnScale(i);
    }

    public int getDisplaySize(int i) {
        return result.getDisplaySize(i);
    }

    public boolean isAutoIncrement(int i) {
        return result.isAutoIncrement(i);
    }

    public int getNullable(int i) {
        return result.getNullable(i);
    }

    public void setFetchSize(int fetchSize) {
        result.setFetchSize(fetchSize);
    }

    public int getFetchSize() {
        return result.getFetchSize();
    }

}
