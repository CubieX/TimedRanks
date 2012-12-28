package com.github.CubieX.TimedRanks;

public final class TimedRanksUtils
{
   public static boolean tryParseInt(String value)  
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

}
