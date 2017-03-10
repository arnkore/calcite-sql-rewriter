package org.apache.calcite.adapter.jdbc;

import com.google.common.collect.ImmutableList;
import org.apache.calcite.adapter.jdbc.tools.JdbcRelBuilder;
import org.apache.calcite.adapter.jdbc.tools.JdbcRelBuilderFactory;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptPlanner;
import org.apache.calcite.plan.RelOptSchema;
import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.rex.RexInputRef;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class JournalledJdbcTableTest {
	private RelOptTable.ToRelContext context;
	private JdbcRelBuilder relBuilder;
	private JdbcTable journalTable;
	private RelOptTable relOptTable;
	private JournalledJdbcTable table;

	@Before
	@SuppressWarnings("ResultOfMethodCallIgnored") // Mockito syntax
	public void setupMocks() {
		relBuilder = Mockito.mock(JdbcRelBuilder.class);
		journalTable = Mockito.mock(JdbcTable.class);
		relOptTable = Mockito.mock(RelOptTable.class);
		context = Mockito.mock(RelOptTable.ToRelContext.class);

		JdbcRelBuilderFactory relBuilderFactory = Mockito.mock(JdbcRelBuilderFactory.class);
		RelOptCluster relOptCluster = Mockito.mock(RelOptCluster.class);
		JournalledJdbcSchema schema = Mockito.mock(JournalledJdbcSchema.class);
		RelOptSchema relOptSchema = Mockito.mock(RelOptSchema.class);
		RexInputRef versionField = Mockito.mock(RexInputRef.class);
		RexInputRef subsequentVersionField = Mockito.mock(RexInputRef.class);

		Mockito.doReturn(Schema.TableType.TABLE).when(journalTable).getJdbcTableType();
		table = new JournalledJdbcTable("theView", schema, journalTable, new String[] {"key1", "key2"});
		table.relBuilderFactory = relBuilderFactory;

		Mockito.doReturn("myV").when(schema).getVersionField();
		Mockito.doReturn("mySV").when(schema).getSubsequentVersionField();
		Mockito.doReturn(relOptCluster).when(context).getCluster();
		Mockito.doReturn(relBuilder).when(relBuilderFactory).create(Mockito.same(relOptCluster), Mockito.same(relOptSchema));
		Mockito.doReturn(Mockito.mock(RelOptPlanner.class)).when(relOptCluster).getPlanner();
		Mockito.doReturn(relOptSchema).when(relOptTable).getRelOptSchema();
		Mockito.doReturn(ImmutableList.of("theSchema", "theView")).when(relOptTable).getQualifiedName();
		Mockito.doReturn(new SqlIdentifier(
				ImmutableList.of("wrongSchema", "theJournal"),
				null,
				new SqlParserPos(0, 0),
				null
		)).when(journalTable).tableName();
		Mockito.doReturn(versionField).when(relBuilder).field(Mockito.eq("myV"));
		Mockito.doReturn(subsequentVersionField).when(relBuilder).field(Mockito.eq("mySV"));
	}

	@Test
	public void testToRel_changesTableToJournal() {
		table.toRel(context, relOptTable);

		Mockito.verify(relBuilder).scanJdbc(Mockito.same(journalTable), Mockito.any());
	}
}
