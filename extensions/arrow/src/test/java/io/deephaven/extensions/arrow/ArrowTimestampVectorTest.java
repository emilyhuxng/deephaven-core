/*
 * Copyright (c) 2016-2023 Deephaven Data Labs and Patent Pending
 */
/*
 * ---------------------------------------------------------------------------------------------------------------------
 * AUTO-GENERATED CLASS - DO NOT EDIT MANUALLY - for any changes edit GenerateArrowColumnSourceTests and regenerate
 * ---------------------------------------------------------------------------------------------------------------------
 */
package io.deephaven.extensions.arrow;

import io.deephaven.chunk.WritableObjectChunk;
import io.deephaven.chunk.attributes.Values;
import io.deephaven.engine.table.ChunkSource;
import io.deephaven.engine.table.ColumnSource;
import io.deephaven.engine.table.impl.QueryTable;
import io.deephaven.engine.testutil.junit4.EngineCleanup;
import io.deephaven.time.DateTime;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.mutable.MutableInt;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

public class ArrowTimestampVectorTest {
    private static final List<Long> expectedRows = Arrays.asList(0L, 1L, 2L, 4L, 8L, 9L);

    private static final DateTime[] expectedValues = new DateTime[] {new DateTime(1670443801), new DateTime(1570443532), null, new DateTime(0), new DateTime(170443801), new DateTime(-72309740)};

    @Rule
    public final EngineCleanup framework = new EngineCleanup();

    private static QueryTable loadTable() {
        //noinspection ConstantConditions;
        final File dataFile = new File(ArrowTimestampVectorTest.class.getResource("/timestamp_vector.arrow").getFile());
        return ArrowWrapperTools.readFeather(dataFile.getPath());
    }

    @Test
    public void testReadArrowFile() {
        final QueryTable table = loadTable();
        Assert.assertEquals(expectedValues.length, table.intSize());

        // check that the expected rows are present;
        final List<Long> actualRows = new ArrayList<>();
        table.getRowSet().forAllRowKeys(actualRows::add);
        Assert.assertEquals(expectedRows, actualRows);

        Assert.assertEquals(1, table.getColumnSources().size());
        // noinspection OptionalGetWithoutIsPresent, unchecked;
        final ColumnSource<DateTime> cs = (ColumnSource<DateTime>)table.getColumnSources().stream().findFirst().get();

        ArrowWrapperTools.Shareable.resetNumBlocksLoaded();
        final MutableInt pos = new MutableInt();
        table.getRowSet().forAllRowKeys(rowKey -> Assert.assertEquals(expectedValues[pos.getAndIncrement()], cs.get(rowKey)));
        Assert.assertEquals(3, ArrowWrapperTools.Shareable.numBlocksLoaded());
    }

    @Test
    public void testFillChunk() {
        final QueryTable table = loadTable();

        // noinspection OptionalGetWithoutIsPresent, unchecked;
        final ColumnSource<DateTime> cs = (ColumnSource<DateTime>)table.getColumnSources().stream().findFirst().get();

        try (final ChunkSource.FillContext fillContext = cs.makeFillContext(table.intSize());
            final WritableObjectChunk<DateTime, Values> chunk = WritableObjectChunk.makeWritableChunk(table.intSize())) {

            ArrowWrapperTools.Shareable.resetNumBlocksLoaded();
            cs.fillChunk(fillContext, chunk, table.getRowSet());
            Assert.assertEquals(3, ArrowWrapperTools.Shareable.numBlocksLoaded());

            for (int ii = 0; ii < expectedValues.length; ++ii) {
                Assert.assertEquals(expectedValues[ii], chunk.get(ii));
            }
        }
    }
}
