package ca.mcgill.cs.swevo.dscribe.generation.doc;

public class CustomInfoFragment implements InfoFragment
{
	private final String fragment;

	public CustomInfoFragment(String fragment)
	{
		this.fragment = fragment;
	}

	@Override
	public FragmentType type()
	{
		return FragmentType.CUSTOM;
	}

	@Override
	public String print()
	{
		return fragment;
	}

}
