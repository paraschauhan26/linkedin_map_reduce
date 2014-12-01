package edu.neu.ccs.datamodelbuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.apache.log4j.Logger;

import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

import com.google.common.collect.Iterators;
import com.google.gson.Gson;

import edu.neu.ccs.constants.Constants;
import edu.neu.ccs.constants.Constants.ClassLabel;
import edu.neu.ccs.objects.Position;
import edu.neu.ccs.objects.Sector;
import edu.neu.ccs.objects.UserProfile;
import edu.neu.ccs.util.UtilHelper;

public class DataModelReducer extends Reducer<Text, UserProfile, NullWritable, Text> {

	private static Logger logger = Logger.getLogger(DataModelReducer.class);
	
	private MultipleOutputs<NullWritable, Text> multipleOutputs;
	private Map<String, List<String>> topTagsPerSector;
	private String topTagsPerSectorFile;
	private Gson gson;
	private FastVector wekaAttributes;
	private Map<String, Integer> tagAttributeMap;
	private Instances trainingSet;
	private int index;
	
	//data model attributes
	private ClassLabel classLabel;
	
	@Override
	protected void setup(Context context) throws IOException, InterruptedException {
		multipleOutputs = new MultipleOutputs<NullWritable, Text>(context);
		gson = new Gson();
		tagAttributeMap = new HashMap<String, Integer>();
		
		topTagsPerSectorFile = Constants.TOP_TAGS_SECTOR + System.currentTimeMillis();
		FileSystem.get(context.getConfiguration()).copyToLocalFile(new Path(Constants.TOP_TAGS_SECTOR), new Path(topTagsPerSectorFile));
		UtilHelper.populateKeyValues(topTagsPerSector, topTagsPerSectorFile);
	}

	@Override
	protected void reduce(Text key, Iterable<UserProfile> values, Context context)
			throws IOException, InterruptedException {
		
		// outputs pruned data
		if (key.toString().contains(Constants.PRUNED_DATA)) {

			for (UserProfile userProfile : values) {
				
				multipleOutputs.write(Constants.PRUNED_DATA_TAG, NullWritable.get(), new Text(gson.toJson(userProfile)));
			}	
			return;
		}
		
		String keyValues[] = key.toString().split(Constants.COMMA);
		String year = keyValues[0];
		String sector = keyValues[1];

		if (!year.equals(context.getConfiguration().get(Constants.TEST_YEAR, "2012"))) {

			createModelStructure(sector);

			trainingSet = new Instances("trainingSet", wekaAttributes, Iterators.size(values.iterator()));
			trainingSet.setClassIndex(index - 1);

			Instance data = new Instance(index);
			for (UserProfile userProfile : values) {

				int currentIndex = 0;
				Set<String> tags = populateTagsAndSetClassifier(userProfile, year);

				if (tags.size() > 0) {

					data.setValue((Attribute) wekaAttributes.elementAt(currentIndex),
							Integer.parseInt(userProfile.getNumOfConnections()));
					currentIndex++;

					for (Entry<String, Integer> entry : tagAttributeMap.entrySet()) {
						if (tags.contains(entry.getKey())) {
							data.setValue((Attribute) wekaAttributes.elementAt(tagAttributeMap.get(entry.getKey())),
									ClassLabel.YES.toString());
						} else {
							data.setValue((Attribute) wekaAttributes.elementAt(tagAttributeMap.get(entry.getKey())),
									ClassLabel.NO.toString());
						}
						currentIndex++;
					}

					data.setValue((Attribute) wekaAttributes.elementAt(currentIndex), sector);

					currentIndex++;

					data.setValue((Attribute) wekaAttributes.elementAt(currentIndex), userProfile.getRelevantExperience());
					currentIndex++;

					data.setValue((Attribute) wekaAttributes.elementAt(currentIndex),classLabel.toString());
					currentIndex++;

					trainingSet.add(data);
				}
			}
			
			try {
				//outputs the DataModel
				multipleOutputs.write(Constants.DATA_MODEL_TAG, NullWritable.get(), new Text(year + Constants.COMMA + sector + 
						Constants.COMMA + new String(getClassifier())));
			} catch (Exception e) {

				logger.error(e);
			}
		}
		else {
			//Test data
			for (UserProfile userprofile : values) {

				//outputs the test data
				multipleOutputs.write(Constants.TEST_DATA_TAG, NullWritable.get(), new Text(gson.toJson(userprofile)));
			}
		}

		tagAttributeMap.clear();
	}

	private String getClassifier() throws Exception {
		
		Classifier cModel = (Classifier) new NaiveBayes();
		cModel.buildClassifier(trainingSet);
		return UtilHelper.serialize(cModel);
	}

	private Set<String> populateTagsAndSetClassifier(UserProfile userProfile, String year) {
		
		List<Position> positions = new ArrayList<Position>();
		Set<String> tags = new HashSet<String>();
		
		for(Position position: userProfile.getPositions()) {

			tags.add(position.getTitle());
		}
		tags.addAll(userProfile.getSkillSet());
		
		if (positions.size() >= 2) {
			
			classLabel = ClassLabel.YES;
		}
		else if (positions.size() == 1) {
			
			classLabel = ClassLabel.NO;
		}

		return tags;
	}

	private void createModelStructure(String sector) {
		
		List<String> tags = topTagsPerSector.get(sector);

		index = 0;

		Attribute numberOfConnections = new Attribute("numberOfConnections");
		index++;

		List<Attribute> skills = new ArrayList<Attribute>();
		Attribute skill = null;
		FastVector skillVector = null;
		for (String tag : tags) {
			
			skillVector = new FastVector(2);
			skillVector.addElement(ClassLabel.YES);
			skillVector.addElement(ClassLabel.NO);
			skill = new Attribute(tag, skillVector);
			skills.add(skill);
			tagAttributeMap.put(tag, index);
			index++;
		}

		Sector[] sectors = Sector.values();

		FastVector sectorVector = new FastVector(sectors.length);
		for (int i = 0; i < sectors.length; i++) {
			
			sectorVector.addElement(sectors[i].name());
		}
		Attribute sectorAttribute = new Attribute("sector", sectorVector);
		index++;
		
		Attribute experience = new Attribute("experience");
		index++;
		
		FastVector classVariable = new FastVector(2);
		classVariable.addElement(ClassLabel.YES);
		classVariable.addElement(ClassLabel.NO);
		Attribute classAttribute = new Attribute("label", classVariable);
		index++;

		wekaAttributes = new FastVector(index);
		wekaAttributes.addElement(numberOfConnections);
		for (Attribute skillAttr : skills) {
			
			wekaAttributes.addElement(skillAttr);
		}
		wekaAttributes.addElement(sectorAttribute);
		wekaAttributes.addElement(experience);
		wekaAttributes.addElement(classAttribute);
	}

	@Override
	protected void cleanup(Context context) throws IOException, InterruptedException {
		
		super.cleanup(context);

		new File(topTagsPerSectorFile).delete();
		
	}
}