package 排序;
//局部的排序和数组合并
public class 归并 {
        public static void main(String[] args){
            int[] arr={1,4,2,5,6,8,7};
            int i=0,j= arr.length-1;
            parttition(arr,i,j);
            for(int a:arr){
                System.out.println(a);
            }
        }
        public static void parttition(int[] num,int low,int hight){
            if(hight<=low) return;
            int index=(hight+low)/2;
            parttition(num,low,index);
            parttition(num,index+1,hight);
            merge(num,low,index,hight);
        }

        public static void merge(int[] a,int low,int index,int hight){
            int x=hight-low;
            int[] arr=new int[hight-low+1];
            int i=index,j=hight;

            while(i>=low && j>=index+1){
                if(a[j]>a[i]){
                    arr[x--]=a[j--];
                }else {
                    arr[x--]=a[i--];
                }
            }
            while (j>=index+1){
                arr[x--]=a[j--];
            }
            while (i>=low){
                arr[x--]=a[i--];
            }
            System.arraycopy(arr,0, a, low, arr.length);
        }
}
