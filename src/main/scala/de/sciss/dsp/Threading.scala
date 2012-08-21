package de.sciss.dsp

import edu.emory.mathcs.utils.ConcurrencyUtils

object Threading {

   /**
    * Use the optimal number of threads (equal to the number of cores reported for the CPU)
    */
   case object Multi extends Threading {
      private[dsp] def setJTransforms() {
         ConcurrencyUtils.setNumberOfThreads( ConcurrencyUtils.getNumberOfProcessors )
      }
   }

   /**
    * Use only single threaded processing
    */
   case object Single extends Threading {
      private[dsp] def setJTransforms() {
         ConcurrencyUtils.setNumberOfThreads( 1 )
      }
   }

   /**
    * Use a custom number of threads.
    */
   final case class Custom ( numThreads: Int ) extends Threading {
      private[dsp] def setJTransforms() {
         ConcurrencyUtils.setNumberOfThreads( numThreads )
      }
   }
}
sealed trait Threading { private[dsp] def setJTransforms() : Unit }
