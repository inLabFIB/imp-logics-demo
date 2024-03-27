# DEMO GUIDE

## Run docker

!You need to have the tool docker installed and running!

Execute the command and open the link `http://localhost:8888/?token=?????` in the browser (the link will be obtained in
the command line after running the command!)

```shell
docker run -p 8888:8888 inlabfib/jupyter-notebook-java-17
```

## Load files

Once in the jupyter notebook environment you will need to load the following files:

- `imp-logics-demo-script.ipynb` : the demo script
- `imp-logics-2.0.0-SNAPSHOT.jar` : IMP-LOGICS .jar dependency
- `imp-logics-2.0.0-SNAPSHOT-tests.jar` : IMP-LOGICS tests .jar dependency
- `???rewriting???` : Rewriting .jar dependency

## Try demo

All set, you can now open the script `imp-logics-demo-script.ipynb` in the jupyter notebook environment and follow the
demo steps.