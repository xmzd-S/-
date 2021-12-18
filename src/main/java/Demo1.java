import org.junit.jupiter.api.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.inverse.InvertMatrix;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Demo1 {
    public static void main(String[] args) throws IOException {
        //建立对文件的读
        String path="";
        Scanner scanner = new Scanner(System.in);
        System.out.println("输入路径");
        path= scanner.next();
        //获取原子对象
        ArrayList<Atom> atomArrayList = getContext(path);
        //获取坐标矩阵
        INDArray positionMatrix = getPositionMatrix(atomArrayList);
        System.out.println("当前文件的坐标矩阵为");
        System.out.println(positionMatrix);

        //更改中心原子为第i+1个
        System.out.println("输入几号原子设为中心");
        int i = scanner.nextInt();
        INDArray newPositionMatrix = changeCenter(positionMatrix, i-1);
        ArrayList<Atom> atomArrayList1 = changeToAtoms(newPositionMatrix, atomArrayList);
        INDArray positionMatrix1 = getPositionMatrix(atomArrayList1);
        System.out.println("更改中心原子为第1个之后的坐标矩阵为");
        System.out.println(positionMatrix1);
        System.out.println("输入三个不同面的原子坐标，以换行结束");
        int[] labels=new int[3];
        int i1 = 0;
        labels[i1++]=scanner.nextInt();
        labels[i1++]=scanner.nextInt();
        labels[i1++]=scanner.nextInt();
        //选取第2个第3个第4个原子向量为基向量
        INDArray indArray = changeBasis(positionMatrix1, labels[0]-1, labels[1]-1, labels[2]-1);
        //转换矩阵的逆矩阵
        INDArray invert = InvertMatrix.invert(indArray, false);

        INDArray mmul = invert.mmul(positionMatrix1);
        ArrayList<Atom> atomArrayList2 = changeToAtoms(mmul, atomArrayList);
        for (Atom atom:atomArrayList2){
            System.out.println(atom);
        }
    }
   //获取文件分子的原子对象
    private static ArrayList<Atom> getContext(String path) throws IOException {
        ArrayList<Atom> atomList = new ArrayList<>();
        FileReader fileReader = new FileReader(path);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        //截取字符串
        String contextLine ="";
        while ((contextLine=bufferedReader.readLine())!=null){
            String[] split = contextLine.split("\\s+");
            if (split.length==5){
                Atom atom = new Atom(split[1],Double.valueOf(split[2]),Double.valueOf(split[3]),Double.valueOf(split[4]));
                atomList.add(atom);
            }

        }
        bufferedReader.close();
        fileReader.close();
        return atomList;
    }
    //获取原子对象的坐标矩阵
    private static INDArray getPositionMatrix( ArrayList<Atom> atomArrayList){
        //存储原子的列向量
        ArrayList<INDArray> indArrayArrayList=new ArrayList<>();
        //把原子的坐标弄成列向量
        for (int i = 0; i<atomArrayList.size();i++){
            INDArray indArray = Nd4j.create(new double[]{atomArrayList.get(i).getX(), atomArrayList.get(i).getY(), atomArrayList.get(i).getZ()}, new int[]{3, 1});
            indArrayArrayList.add(indArray);
        }
        //列向量按行进行链接
        INDArray[] indArrays = new INDArray[atomArrayList.size()];
        indArrays = indArrayArrayList.toArray(indArrays);
        INDArray hstack = Nd4j.hstack(indArrays);

        return hstack;
    }
    //将第几个原子设为中心而基不变
    private static INDArray changeCenter(INDArray positionMatrix,int i){
        INDArray indArray = positionMatrix.sub(positionMatrix.getColumn(i).reshape(3,1));
        return indArray;
    }
    //根据坐标矩阵还原为原子对象
    private static ArrayList<Atom> changeToAtoms(INDArray positionMatrix,ArrayList<Atom> atomArrayList){
        ArrayList<Atom> newAtomArrayList = new ArrayList<>();
        for (int i = 0;i<positionMatrix.columns();i++){
            INDArray column = positionMatrix.getColumn(i);
            Atom atom = new Atom(atomArrayList.get(i).getSymbol(),column.getDouble(0,0),column.getDouble(1,0),column.getDouble(2,0));
            newAtomArrayList.add(atom);
        }

        return newAtomArrayList;
    }
    //选取其中三个非中心原子,num为原子的序号，从1开始，作为基并且施密特正交化然后归一化
    private static INDArray changeBasis(INDArray positionMatrix,int num1,int num2,int num3){
        System.out.println("选取第"+(num1+1)+"个,第"+(num2+1)+"个,第+"+(num3+1)+"个原子向量为基向量");
        INDArray column1 = positionMatrix.getColumn(num1).dup().reshape(3,1);
        INDArray column2 = positionMatrix.getColumn(num2).dup().reshape(3,1);
        INDArray column3 = positionMatrix.getColumn(num3).dup().reshape(3,1);
        INDArray B1 = column1;

        INDArray B2 =column2.sub(column2.transpose().dup().mmul(B1) .div(B1.transpose().dup().mmul(B1)).mulColumnVector(B1.dup())) ;

        INDArray B3 = column3.sub(
                column3.transpose().dup().mmul(B1) .div(B1.transpose().dup().mmul(B1)).mulColumnVector(B1.dup()))
                .sub(
                        column3.transpose().dup().mmul(B2) .div(B2.transpose().dup().mmul(B2)).mulColumnVector(B2.dup()));

        System.out.println("所选基向量进行施密特正交化");
        System.out.println( Nd4j.hstack(B1, B2, B3));
        double[] B12 = B1.transpose().dup().mmul(B1).toDoubleVector();
        double[] B22 = B2.transpose().dup().mmul(B2).toDoubleVector();
        double[] B32 = B3.transpose().dup().mmul(B3).toDoubleVector();

        B1=B1.div(Math.pow(B12[0],0.5));
        B2=B2.div(Math.pow(B22[0],0.5));
        B3=B3.div(Math.pow(B32[0],0.5));
        //因为B1的位置在第一列，导致X轴坐标不为0，YZ轴为0
        INDArray hstack = Nd4j.hstack(B1, B2, B3);

        System.out.println("所选基向量进行归一化(转换矩阵)");
        System.out.println( Nd4j.hstack(B1, B2, B3));

        return  hstack;

    }

}
