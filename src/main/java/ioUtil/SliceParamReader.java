package ioUtil;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import slice.Constant;

public class SliceParamReader {
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		List<SliceParam> sliceParam = new SliceParamReader().readSliceParam(Constant.SLICEPARAM);
		System.out.println(sliceParam.get(0).getCommitID());
	}
	public List<SliceParam> readSliceParam(String filePath) throws IOException{
		List<SliceParam> sliceParam = new ArrayList<SliceParam>();
		Path path = Paths.get(filePath);
		Charset charset = Charset.forName("ISO-8859-1");
		List<String> lines= Files.readAllLines(path,charset);
		SliceParam sParam = new SliceParam();
		for(String line : lines){
			String[] splits = line.split("#");
			sParam.setCommitID(splits[0]);
			sParam.setMethodExtract(splits[1]);
			sParam.setMethodEntry(splits[2]);
			sParam.setClassName(splits[3]);
			sParam.setInvokeLine(Integer.parseInt(splits[6]));
			sliceParam.add(sParam);
		}
		return sliceParam;
	}
}
