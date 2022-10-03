/* the following three declarations are for use of the random number 
   generator rand and the associated functions randst and randgt for 
   seed management. This file (rand.h) should be included in any program
   using these functions */
float randno(int stream);
void randst(long zset, int stream);
long randgt(int stream);

