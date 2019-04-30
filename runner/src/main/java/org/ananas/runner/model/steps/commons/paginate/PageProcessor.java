package org.ananas.runner.model.steps.commons.paginate;

import org.ananas.runner.model.errors.DatumaniaException;
import org.ananas.runner.model.errors.ExceptionHandler;
import org.apache.commons.lang3.tuple.MutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.List;

public class PageProcessor {

	private static final Logger LOG = LoggerFactory.getLogger(PageProcessor.class);

	public interface ParDo<T> {
		T process(String element, int i);
	}

	/**
	 * Read file in memory
	 *
	 * @param path     the File path
	 * @param page     The page number
	 * @param pageSize The page size
	 * @return a list of lines
	 * @throws IOException
	 */
	public static <T> List<T> readFile(String path,
									   Integer page,
									   Integer pageSize,
									   ParDo<T> lambdaFunction) {
		try {
			List<T> linkedList = new LinkedList<>();
			File f = new File(path);

			long counter = 1;
			long startT = System.currentTimeMillis();

			FileInputStream fis = new FileInputStream(f);
			FileChannel fc = fis.getChannel();

			MappedByteBuffer mmb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());

			byte[] buffer = new byte[(int) fc.size()];
			mmb.get(buffer);


			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer);
			BufferedReader in = new BufferedReader(new InputStreamReader(byteArrayInputStream));
			String line;
			T o;
			int i = 0;
			long start = page * pageSize;
			long end = (page + 1) * pageSize;
			for (; counter < end; counter++) {
				line = in.readLine();
				if (line == null) {
					break;// end of file if line is null
				}
				if (counter >= start) {
					o = lambdaFunction.process(line, i++);
					if (o != null) {
						linkedList.add(o);
					}
				}
			}

			in.close();
			byteArrayInputStream.close();
			fis.close();

			long endT = System.currentTimeMillis();
			long tot = endT - startT;
			LOG.debug(String.format("No Of Message %s , Time(ms) %s ", counter, tot));
			return linkedList;
		} catch (IOException e) {
			throw new DatumaniaException(
					MutablePair.of(ExceptionHandler.ErrorCode.GENERAL,
							"An error occurred while reading file : " + e.getMessage()));
		}
	}


}
