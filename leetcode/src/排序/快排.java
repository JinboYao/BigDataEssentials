package 排序;

public class 快排 {
    public static void main(String[] args){
        int[] arr={1,4,2,5,6,8,7};
        int i=0,j= arr.length-1;
        quicksort(arr,i,j);
        System.out.println(arr);
        for(int a:arr){
            System.out.print(a);
        }
    }
    public static void quicksort(int[] arr,int start,int end){
        if(start>=end) return;
        int index=arr[start];
        int i=start,j=end;
        while (i<j){
            while(i<j&& arr[j]>=index){
                j--;
            }
            while (i<j&& arr[i]<=index){
                i++;
            }
            if(i<j){
                swap(arr,i,j);
            }
        }
        swap(arr,i,start);
        quicksort(arr,start,i-1);
        quicksort(arr,i+1,end);
    }
    public static void swap(int[] arr,int i,int j){
        int temp=arr[i];
        arr[i]=arr[j];
        arr[j]=temp;
    }
}
