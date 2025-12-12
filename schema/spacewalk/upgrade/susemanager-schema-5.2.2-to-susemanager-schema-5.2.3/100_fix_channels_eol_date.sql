UPDATE rhnchannel SET end_of_life = null WHERE end_of_life <= TO_TIMESTAMP(3600*24);
