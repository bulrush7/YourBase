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
package com.codefollower.yourbase.table;

import com.codefollower.h2.api.TableEngine;
import com.codefollower.h2.command.ddl.CreateTableData;
import com.codefollower.h2.engine.Database;
import com.codefollower.h2.table.RegularTable;
import com.codefollower.h2.table.TableBase;

/**
 * 
 * 用于支持标准的CREATE TABLE，这种类型的表被看成是静态类型的，不需要定义列族，字段类型必须事先定义，并且必须有主键。
 *
 */
public class HBaseTableEngine implements TableEngine {

    @Override
    public TableBase createTable(CreateTableData data) {
        Database db = data.session.getDatabase();
        if (!data.isHidden && !data.temporary && data.id > 0 //
                && !db.isPersistent() && !db.getShortName().toLowerCase().startsWith("management_db_"))
            return new HBaseTable(data);
        else
            return new RegularTable(data);
    }

}
