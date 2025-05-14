package util;

public class Map
{
	public int A;
	public int B;
	public int N;
	public char[][] map;

	public Map()
	{
		this.A = 0;
		this.B = 0;
		this.N = 0;
	}

	public void setMapAttr(int A, int B, int N)
	{
		this.A = A;
		this.B = B;
		this.N = N;
		this.map = new char[A + 1][B + 1]; 
	}

	public void setMap(char[][] map)
	{
		for(int i = 0; i < this.A + 1; i++)
		{
			for(int j = 0; j < this.B + 1; j++)
			{
				if(j >= map[i].length)
				{
					this.map[i][j] = ' ';
				}
				else
				{
					this.map[i][j] = map[i][j];
				}
			}
		}
	}
	
}
