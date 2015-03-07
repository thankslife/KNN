package knntext;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;


public class MyKNN {
	private static String defaultPath = "E:\\java_Eclipse\\NBM2\\Reduced";
	private static File traningTextDir;// ��Ŵ�ѵ���ı���Ŀ¼
	private static String[] traningFileClassifications;//ÿƪ���µ��ļ���
	private static SaveWord[] saveword;//�洢����ѵ�����µķִ�
	private static SaveWord[] _test_text;//�洢���еĴ��������µķִ�
	private static int _allnum;//��¼һ���м�ƪѵ������
	private static int K;
	private static HashMap<String, Double> idf;
	private static HashSet<String> _allWordSet;
	//private static HashMap<String, Double> tempHashMap;//����һ��ģ���Ա����ͳ����ѵ�������зִ�Ϊά�ȹ����������ռ�ģ���¸���ѵ�����µĴ�Ƶ
	private static double right;//��ȷ��
	
	public static void install() throws IOException
    {
		_allWordSet = new HashSet<String>();
		idf = new HashMap<String,Double>();
		K = 18;
		right = 0.0d;
        traningTextDir = new File(defaultPath);
        if (!traningTextDir.isDirectory())
        {
            throw new IllegalArgumentException("ѵ�����Ͽ�����ʧ�ܣ� [" +defaultPath + "]");
        }
        traningFileClassifications = traningTextDir.list();//���г�ѵ�������������
        File tempFile;
        for(int i = 0; i < traningFileClassifications.length; i++){
        	tempFile = new File(defaultPath + File.separator + traningFileClassifications[i]);
        	_allnum = _allnum + tempFile.list().length;
        } 
    }
	
	public static void installword() throws IOException{//��ÿƪѵ�����·ִʺ�洢��hashset��
		saveword = new SaveWord[_allnum];
		int index = 0;
		NlpirMethod.Nlpir_init();
		for (int i = 0; i < traningFileClassifications.length; i++) {
			File tempFile = new File(defaultPath + File.separator
					+ traningFileClassifications[i]);
			String[] ret = tempFile.list();
			for (int j = 0; j < ret.length; j++) {
				InputStreamReader isReader = new InputStreamReader(
						new FileInputStream(traningTextDir.getPath()
								+ File.separator
								+ traningFileClassifications[i]
								+ File.separator + ret[j]), "GBK");
				BufferedReader reader = new BufferedReader(isReader);
				String aline;
				StringBuilder sb = new StringBuilder();
				while ((aline = reader.readLine()) != null) {
					sb.append(aline + " ");
				}
				isReader.close();
				reader.close();
				String []temp = NlpirMethod.NLPIR_ParagraphProcess(sb.toString(), 0).split(" ");
				temp = DropStopWords(temp);
				
				saveword[index] = new SaveWord();
				saveword[index].classyString = traningFileClassifications[i];//��¼��ǰ���������ĸ����
				
				for(int k = 0; k < temp.length; k ++){
					if(saveword[index]._wordmap.containsKey(temp[k])){
						//saveword[index].sum_word_of_this_text++;
						double tempnum = saveword[index]._wordmap.get(temp[k]) + 1.0d;
						saveword[index]._wordmap.put(temp[k], tempnum);
					}
					else{
						//saveword[index].sum_word_of_this_text++;
						saveword[index]._wordmap.put(temp[k], 1.0d);
					}
						
				}
				for(int k = 0; k < temp.length; k++){
					double tempnum = saveword[index]._wordmap.get(temp[k])/temp.length;
					saveword[index]._wordmap.put(temp[k], tempnum);
				}
				_allWordSet.addAll(saveword[index]._wordmap.keySet());
				index++;
			}

		}//forѭ������saveword�д洢���������µĴ�Ƶ,_allWordSet�д洢�����е�ѵ�����µĴ�
		
		/*Object[] tempStrings = _allWordSet.toArray();
		tempHashMap = new HashMap<String,Double>();
		for(int i= 0; i < tempStrings.length; i++){
			tempHashMap.put((String) tempStrings[i], 0.0d);
		}//����һ��ģ���Ա����ͳ����ѵ�������зִ�Ϊά�ȹ����������ռ�ģ���¸���ѵ�����µĴ�Ƶ	
		System.out.println("ģ�幹�����");*/
		
		/*for(int i = 0; i < saveword.length; i++){
			HashMap<String, Double> tempsaveword = new HashMap<String,Double>(tempHashMap);
			Set set = saveword[i]._wordmap.keySet();
			Object[] tempset = set.toArray();
			for(int j = 0;j < tempset.length; j++){
				tempsaveword.put((String) tempset[j], saveword[i]._wordmap.get(tempset[j]));
			}
			System.out.println(tempsaveword.size());
			System.out.println(i);
			saveword[i]._wordmap.clear();
			saveword[i]._wordmap = null;
			saveword[i]._wordmap = new HashMap<String,Double>(tempsaveword);
			tempsaveword.clear();  
			tempsaveword=null; 
			System.gc();
		}//�������saveword�洢����ѵ�������зִ�Ϊά�ȹ����������ռ�ģ���¸���ѵ�����µĴ�Ƶ
		*/
		Object[]  _allWordSetStrings = _allWordSet.toArray();
		for(int i = 0; i < _allWordSetStrings.length; i++){
			double num = 0;
			for(int j = 0; j < saveword.length; j++){
				if(saveword[j]._wordmap.containsKey(_allWordSetStrings[i]))
					num++;
			}
			idf.put((String) _allWordSetStrings[i], Math.log(_allnum/num + 0.01));
		}
		
		
		
		
		
		
		System.out.println("ѵ������");
	}
	
	public static void installTestText() throws IOException{//�������в��Լ�
		NlpirMethod.Nlpir_init();
		int index = 0;
		File file = new File("E:\\java_Eclipse\\K_NN_Text\\���Լ�");
    	String []filepathStrings = file.list();
    	int test_text_num = 0;
    	for(int i = 0; i < filepathStrings.length; i++){
    		File file2 = new File("E:\\java_Eclipse\\K_NN_Text\\���Լ�\\" + filepathStrings[i]);
    		String []filepathStrings2 = file2.list();
    		test_text_num = test_text_num + filepathStrings2.length;
    	}//ͳ�Ʋ��Լ��м�ƪ����
		_test_text = new SaveWord[test_text_num];
		
		System.out.println("���Լ���" + test_text_num);
		
    	
    	for(int i = 0; i <filepathStrings.length; i++){
    		File file2 = new File("E:\\java_Eclipse\\K_NN_Text\\���Լ�\\" + filepathStrings[i]);
    		String []filepathStrings2 = file2.list();
    		for(int j = 0; j < filepathStrings2.length; j++){
    			_test_text[index] = new SaveWord();
    			_test_text[index].classyString = filepathStrings[i];////////////////////
    			InputStreamReader isReader =new InputStreamReader(new FileInputStream("E:\\java_Eclipse\\K_NN_Text\\���Լ�\\"+filepathStrings[i] + "\\" + filepathStrings2[j]),"GBK");
    			BufferedReader reader = new BufferedReader(isReader);
    			String aline;
    			StringBuilder sb = new StringBuilder();
    			while ((aline = reader.readLine()) != null)
    			{
    				sb.append(aline + " ");
    				}
    			isReader.close();
    			reader.close();
    			String sSrc = sb.toString();
    			String[] terms = NlpirMethod.NLPIR_ParagraphProcess(sSrc, 0).split(" ");//���ķִʴ���
    			terms = DropStopWords(terms);//ȥ��ͣ�ôʣ�����Ӱ��ִ�
    			for(int k = 0; k < terms.length; k ++){
					if(_test_text[index]._wordmap.containsKey(terms[k])){
						_test_text[index].sum_word_of_this_text++;
						double tempnum = _test_text[index]._wordmap.get(terms[k]) + 1.0d;
						_test_text[index]._wordmap.put(terms[k], tempnum);
					}
					else{
						_test_text[index].sum_word_of_this_text++;
						_test_text[index]._wordmap.put(terms[k], 1.0d);
					}
						
				}
    			for(int k = 0; k < terms.length; k++){
					double tempnum = _test_text[index]._wordmap.get(terms[k])/terms.length;
					_test_text[index]._wordmap.put(terms[k], tempnum);
				}
    			index++;
    		}
    	}//forѭ��������_test_text�ʹ洢�����д������ı��Ĵ�Ƶ
    	System.out.println("���Լ���Ƶͳ�����");
    	
		/*for(int i = 0; i < _test_text.length; i++){
			HashMap<String, Double> tempsaveword = new HashMap<String,Double>(tempHashMap);
			Set set = _test_text[i]._wordmap.keySet();
			Object[] tempset = set.toArray();
			for(int j = 0;j < tempset.length; j++){
				tempsaveword.put((String) tempset[j], _test_text[i]._wordmap.get(tempset[j]));
			}
			_test_text[i]._wordmap = new HashMap<String,Double>(tempsaveword);
			tempsaveword.clear();  
			tempsaveword=null; 
		}*/
		System.out.println("���Լ�Ԥ�������");
	}
	
	public static String[] DropStopWords(String[] oldWords)//---------------------------------------ȥ��ͣ�ô�
    {
        Vector<String> v1 = new Vector<String>();
        for(int i=0;i<oldWords.length;++i)
        {
            if(StopWordsHandler.IsStopWord(oldWords[i])==false)
            {//����ͣ�ô�
                v1.add(oldWords[i]);
            }
        }
        String[] newWords = new String[v1.size()];//��vector������ת�����ַ��������Ա��������
        v1.toArray(newWords);
        return newWords;
    }
	
	@SuppressWarnings("unchecked")
	public static String[] knn(){
		Object[] temp_allWordSet = _allWordSet.toArray();
		String[] resultStrings = new String[ _test_text.length];//�洢����Ľ��
		
		for(int i = 0; i < _test_text.length; i++){
			Set settemp2 = _test_text[i]._wordmap.keySet();
			Object[] t2 = settemp2.toArray();
		//for(int i = 0; i < 1; i++){
			TreeMap<Double, String> ret = new TreeMap<Double,String>();
			for(int j = 0; j < saveword.length; j++){
			//for(int j = 0; j < 1; j++){
				double sum1 = 0.0d;//�������ƶȹ�ʽ�ķ���
				double sum2 = 0.0d;//�������ƶȵķ�ĸ��һ������
				double sum3 = 0.0d;//�������ƶȵķ�ĸ�ڶ�������
				//double A = 0.0d;
				//double B = 0.0d;
				
				Set settemp = saveword[j]._wordmap.keySet();
				Object[] t1 = settemp.toArray();
				for(int q = 0; q < t1.length; q++){
					sum2 = sum2 + (saveword[j]._wordmap.get(t1[q])*idf.get(t1[q]))*(saveword[j]._wordmap.get(t1[q])*idf.get(t1[q]));
					
				}
				//System.out.println("sum2 = " + sum2);
				for(int q = 0; q < t2.length; q++){
					if(_allWordSet.contains(t2[q])){
						sum3 = sum3 +( _test_text[i]._wordmap.get(t2[q])*idf.get(t2[q]))*(_test_text[i]._wordmap.get(t2[q])*idf.get(t2[q]));
						if(saveword[j]._wordmap.containsKey(t2[q]))
							sum1 = sum1 + (_test_text[i]._wordmap.get(t2[q])*idf.get(t2[q]))*(saveword[j]._wordmap.get(t2[q])*idf.get(t2[q]));
					}
					
				}
				//System.out.println("sum3 = " + sum3);
				//System.out.println("sum1 = " + sum1);
				
				
				
				
				/*for(int q = 0; q < temp_allWordSet.length; q++){
					if(_test_text[i]._wordmap.containsKey(temp_allWordSet[q]))
						A = _test_text[i]._wordmap.get(temp_allWordSet[q]);
					else 
						A = 0.0d;
					if(saveword[j]._wordmap.containsKey(temp_allWordSet[q]))
						B = saveword[j]._wordmap.get(temp_allWordSet[q]);
					else 
						B = 0.0d;
					
					//sum1 = sum1 + _test_text[i]._wordmap.get(temp_allWordSet[q])*saveword[j]._wordmap.get(temp_allWordSet[q]);
					//sum2 = sum2 + _test_text[i]._wordmap.get(temp_allWordSet[q])*_test_text[i]._wordmap.get(temp_allWordSet[q]);
					//sum3 = sum3 + saveword[j]._wordmap.get(temp_allWordSet[q])*saveword[j]._wordmap.get(temp_allWordSet[q]);
					sum1 = sum1 + A * B;
					sum2 = sum2 + A * A;
					sum3 = sum3 + B * B;
				}*/
				
				
				
				
				
				
				/*HashSet<String> set = new HashSet<String>(saveword[j]._wordmap.keySet());
				
				set.addAll(_test_text[i]._wordmap.keySet());//��ʱset�д洢�˴��������ƶȵ���ƪ���µ�������
				
				Object[] wordkey = set.toArray();
				
				for(int  q = 0; q < wordkey.length; q++){
					int is = 0;
					if(_test_text[i]._wordmap.containsKey(wordkey[q]) && saveword[j]._wordmap.containsKey(wordkey[q]))
						is = 4;//�õ��ʳ���������ƪ������
					else if(_test_text[i]._wordmap.containsKey(wordkey[q]) && (!(saveword[j]._wordmap.containsKey(wordkey[q]))))
						is = 3;//�õ��ʳ������˲������¶�û�г�����ѵ������
					else if((!(_test_text[i]._wordmap.containsKey(wordkey[q]))) && saveword[j]._wordmap.containsKey(wordkey[q]))
						is = 2;//�õ��ʳ�����ѵ�����¶�û�г����ڲ�������
					
					switch (is) {
					case 4:
						sum1 = sum1 + _test_text[i]._wordmap.get(wordkey[q])*saveword[j]._wordmap.get(wordkey[q]);
						sum2 = sum2 + _test_text[i]._wordmap.get(wordkey[q])*_test_text[i]._wordmap.get(wordkey[q]);
						sum3 = sum3 + saveword[j]._wordmap.get(wordkey[q])*saveword[j]._wordmap.get(wordkey[q]);
						break;	
					case 3:
						sum1 = sum1;
						sum2 = sum2 + _test_text[i]._wordmap.get(wordkey[q])*_test_text[i]._wordmap.get(wordkey[q]);
						sum3 = sum3 ;
						//System.out.println("qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq");
						break;
					case 2:
						sum1 = sum1;
						sum2 = sum2;
						sum3 = sum3 + saveword[j]._wordmap.get(wordkey[q])*saveword[j]._wordmap.get(wordkey[q]);
						//System.out.println("qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq");
						break;
					default:
						//System.out.println("qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq");
						break;
					}
					
					
				}*/
				//System.out.println(sum1/(Math.sqrt(sum2*sum3)));
				ret.put(sum1/(Math.sqrt(sum2*sum3)), saveword[j].classyString);
			}//forѭ��������ret�б����˵�ǰ���������µ�ÿһ��ѵ�����µľ��벢�Ұ���������
			System.out.println(ret);////////////////////////////////////////////////////////
			
			Set<Double> set = ret.keySet();//ȡ��ret�еļ�ֵ�Խ��к�������
			Object[] key_of_ret = set.toArray();
			//System.out.println(saveword.length);
			//System.out.println(key_of_ret.length);
			System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
			System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
			for(int ii = key_of_ret.length-1; ii > key_of_ret.length-1-K; ii--){
				System.out.println(key_of_ret[ii] + "--" + ret.get(key_of_ret[ii]));
			}
			System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
			System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
			String[] tempStrings = new String[K];
			
			for(int qq = 0; qq < K; qq++){
				tempStrings[qq] = ret.get(key_of_ret[key_of_ret.length-1-qq]);
			}//��ǰK�����µ����ȡ�������浽tempStrings��
			System.out.println("============================================================begin");
			for(int qq = 0; qq < K; qq++){
				System.out.println(tempStrings[qq]);
			}
			System.out.println("===============================================================end");
			final HashMap<String, Double> tempHashMap = new HashMap<String,Double>();
			for(int qq = 0; qq < K; qq++){//ͳ�������Ƶ�kƪ���µ����������
				if(tempHashMap.containsKey(tempStrings[qq])){
					double temp = tempHashMap.get(tempStrings[qq]) + /*1.0d/*/((double)key_of_ret[key_of_ret.length-1-qq]*(double)key_of_ret[key_of_ret.length-1-qq]);
					tempHashMap.put(tempStrings[qq], temp);
				}
				else {
					tempHashMap.put(tempStrings[qq], /*1.0d/*/((double)key_of_ret[key_of_ret.length-1-qq]*(double)key_of_ret[key_of_ret.length-1-qq]));
				}
			}
			
			
			ArrayList keys = new ArrayList(tempHashMap.keySet());//�õ�key����  
	        //��keys���򣬵����أ�Ҫ���պ�������ȽϵĹ���
	        java.util.Collections.sort(keys,new Comparator()//���ؼ�����������������÷����������ʽ��������Եõ������ʵ����
	        {
	            public int compare(final Object o1,final Object o2)
	            {
	                if(Double.parseDouble(tempHashMap.get(o1).toString())<Double.parseDouble(tempHashMap.get(o2).toString()))
	                    return 1;
	               
	                else if(Double.parseDouble(tempHashMap.get(o1).toString())==Double.parseDouble(tempHashMap.get(o2).toString()))
	                    return 0;
	              
	                else
	                    return -1;
	            }
	        });
	        for(int ii = 0; ii < keys.size(); ii++){
	        	System.out.println(tempHashMap.get(keys.get(ii)));
	        }
	        System.out.println( _test_text[i].classyString + "--" + (String)keys.get(0));
	        resultStrings[i] = _test_text[i].classyString + "--" + (String)keys.get(0);
	        if(_test_text[i].classyString.equals(keys.get(0)))
	        	right++;
		}
		return resultStrings;
	}
	
	public static void main(String[] args) throws IOException{
		install();
		System.out.println("exe----install");
		installword();
		System.out.println("exe----installword");
    	installTestText();
    	System.out.println("exe----installTestText");
    	System.out.println(saveword[0]._wordmap);
    	System.out.println(_test_text[0]._wordmap);
    	/*System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
    	for(int i = 0; i < 30; i++){
    		System.out.println(saveword[i]._wordmap);
    	}
    	System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
    	for(int i = 0; i < 30; i++){
    		System.out.println(_test_text[i]._wordmap);
    	}
    	System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
    	*/
    	String[] ret = knn();
    	for(int i = 0; i < ret.length; i++){
    		System.out.println(ret[i]);
    	}
    	
    	System.out.println("��ȷ��Ϊ��" + right/_test_text.length);
	}

}
