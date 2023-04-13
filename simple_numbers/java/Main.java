import java.util.ArrayList;

class Main {
    final static int TARGET = 10_000_000;

    public static void prime() {
        IntArrayList primes = new IntArrayList();
        for (int i = 2; i <= TARGET; ++i)
        {
            boolean flag = false;
            for (int idx = 0; idx < primes.size(); ++idx)
            {
                int prime = primes.get(idx);
                if (i % prime == 0)
                {
                    flag = true;
                    break;
                }
                if (prime > Math.sqrt(i))
                {
                    break;
                }
            }
            if (!flag)
            {
                primes.add(i);
            }
        }
        System.out.println(primes.get(primes.size() - 1));
    }
    public static void main(String[] args) {
        for (int j = 0; j < 100; ++j) {
            prime();
        }
    }
}