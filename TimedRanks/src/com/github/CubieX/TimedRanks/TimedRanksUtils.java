package com.github.CubieX.TimedRanks;

public final class TimedRanksUtils
{
   public static boolean isInteger(String value)  
   {  
      try  
      {  
         Integer.parseInt(value);  
         return true;  
      }
      catch(NumberFormatException nfe)  
      {  
         return false;  
      }  
   }
   
   public static boolean isPositiveInteger( String input )  
   {  
      try  
      {  
         int i = Integer.parseInt( input );

         if(i > 0)
         {
            return true;  
         }
         else
         {
            return false;
         }
      }  
      catch( Exception e)  
      {  
         return false;  
      }  
   }

}
