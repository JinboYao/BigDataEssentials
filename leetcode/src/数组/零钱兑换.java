package 数组;

public class 零钱兑换 {
    public int coinChange(int[] coins, int amount) {
        int[] dp=new int[amount+1];
        dp[0]=0;
        for (int i=1;i<=amount;i++){
            int min=Integer.MAX_VALUE;
            for(int j=0;j<coins.length||coins[j]<i;j++){
                min=Math.min(min,dp[i-coins[j]]);
            }
            dp[i]=min+1;
        }
        return dp[amount];
    }
}
