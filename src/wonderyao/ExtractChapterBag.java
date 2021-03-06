package wonderyao;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.BagFactory;
import org.apache.pig.data.DataBag;
import org.apache.pig.data.DataType;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;
import org.apache.pig.impl.logicalLayer.schema.Schema;
import org.apache.pig.impl.logicalLayer.schema.Schema.FieldSchema;

public class ExtractChapterBag extends EvalFunc<DataBag>{
	private BagFactory bagFactory = BagFactory.getInstance();
	private TupleFactory tupleFactory = TupleFactory.getInstance();
	private int mFirstN = 10;
	public ExtractChapterBag(String firstN){
		try{
			mFirstN = Integer.parseInt(firstN);
		}
		catch(NumberFormatException e){}
	}
	
	/*private String join(ArrayList<String> arr, String delimit){
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<arr.size(); ++i){
			if(i!=0)
				sb.append(delimit);
			sb.append(arr.get(i));
		}
		return sb.toString();
	}*/
	
	private String join(String[] arr, int begin, int end){
		if(end-begin==1){
			return arr[begin];
		}
		else if(end>begin){
			StringBuilder sb = new StringBuilder();
			for(int i=begin; i<end; ++i){
				sb.append(arr[i]);
			}
			return sb.toString();
		}
		else{
			return "";
		}
	}
	
	private String getChapterName(String tx){
		String[] cells = tx.split(",");
		if(cells.length>6){
			if(cells[cells.length-1].indexOf(';')>=0){
				return join(cells, 5, cells.length-1);
			}
			else{
				return join(cells, 5, cells.length);
			}
		}
		else if(cells.length==6){
			return cells[5];
		}
		else{
			return "";
		}
	}
	
	@Override
	public DataBag exec(Tuple input) throws IOException {
		DataBag result = bagFactory.newDefaultBag();
		String chaptersStr = (String)input.get(0);
		if(chaptersStr!=null){
			String[] chapters = chaptersStr.split("\\|");
			for(int i=0; i<chapters.length && i<mFirstN; ++i){
				Tuple tpl = tupleFactory.newTuple(1);
				tpl.set(0, getChapterName(chapters[i]));
				result.add(tpl);
			}
		}
		return result;
	}
	
	public Schema outputSchema(Schema input){
		try{
			if(input.getFields().size() != 1 ||
				input.getField(0).type != DataType.CHARARRAY){
				throw new RuntimeException("chapters:chararray)");
			}
			Schema tpl = new Schema();
			tpl.add(new FieldSchema("chapter", DataType.CHARARRAY));
			FieldSchema bagschema = new FieldSchema("chapters", DataType.BAG);
			bagschema.schema = tpl;
			Schema result = new Schema();
			result.add(bagschema);
			return result;
		}
		catch(Exception e){
			throw new RuntimeException(e);
		}
		
	}
}
