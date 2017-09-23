using System.Collections;
using System.Collections.Generic;
using UnityEngine;

/*
* this script switches between cameras based on the rotation of the models in the scene
* It is currently unused
*/
public class trajectory : MonoBehaviour
{

    float orbitVelocity = 30.0f;	//arbitrary value which determines the speed of rotation of the models
    public Camera[] cameraList;	//list of cameras in the scene. This is explosed as a public variable to allow dragging and dropping cameras in the editor
    private int cameraStep = 0;	//this variable stores the angle in degrees that each camera should cover.

    void Start ()
    {
		cameraStep = 360 / cameraList.Length;	//compute the angle (in degrees) that each camera should cover before switching to the next camera
		//For instance, if there are 8 cameras in the scene, then given the circular trajectory, each camera coveres 360/8 = 45 degrees
    }

    void Update ()
    {
        gameObject.transform.Rotate(0, orbitVelocity * Time.deltaTime, 0);	//this rotates the models about a fixed circular orbit
        switchCamera(transform.rotation.eulerAngles.y);	//this switches camera based on the rotation of the models around the y axis
    }

    void switchCamera(float currentRotation)
    {
        int camIndexToEnable = (int) currentRotation / cameraStep;	//determine which camera to enable based on the rotation of the models
        for (int i=0;i<cameraList.Length;i++)	//iterate through camera list, enable the appropriate camera and disable all others
        {
            if(i == camIndexToEnable)
            {
                cameraList[i].enabled = true;
            }
                
            else
            {
                cameraList[i].enabled = false;
            }
        }
    }
}
