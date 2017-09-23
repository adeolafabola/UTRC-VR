using System.Collections;
using System.Collections.Generic;
using UnityEngine;

/*
* this script is used to randomly move and rotate the models
*/
public class MoveAndTrack : MonoBehaviour 
{
	public Vector3 randomDirection;
	void Start () 
	{
		randomDirection = new Vector3(Random.value, Random.value, Random.value);
	}

	void Update () 
	{
		gameObject.transform.Translate (randomDirection*Time.deltaTime);	//move each model in a random direction
		gameObject.transform.RotateAround (Vector3.zero, Vector3.up, 3);	//rotate each model about the origin
	}
}
