using System.Collections;
using System.Collections.Generic;
using UnityEngine;

/*
* this script makes the cameras to track the position of the cube
* It is currently unused
*/
public class LookAtObject : MonoBehaviour
{

    public Transform target;

    void Start ()
    {
		
	}
	
	void Update ()
    {
        transform.LookAt(target);	//this ensures that the camera focuses on the cube target
    }
}
