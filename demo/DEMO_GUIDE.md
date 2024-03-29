# DEMO GUIDE

The demo is run over a Jupyter notebook (https://jupyter.org/).
To facilitate its usage, we have already prepared a docker-image
with everything you need.

## Run docker

1. Ensure that you have the tool docker installed and running.

2. Execute the following command:

```shell
docker run -p 8888:8888 inlabfib/jupyter-notebook-java-17
```

You'll see that the command prints a link with the form `http://localhost:8888/?token=?????`.

3. Open the previously obtained link in the browser.

## Load files

Once in the jupyter notebook environment you will need to load the following files:

- `/demo/DB_Demo.png` : an image representing a logic schema we will use
- `/demo/DatalogMetamodel.png` : an image of the Datalog metamodel
- `/demo/DependencyMetamodel.png` : an image of the Dependency metamodel
- `/demo/imp-logics-demo-script.ipynb` : the demo script
- `/lib/imp-logics-2.0.0.jar` : IMP-Logics .jar dependency
- `/lib/imp-logics-2.0.0-tests.jar` : IMP-Logics tests .jar dependency
- `/lib/ontological-queries-rewriting-1.0-SNAPSHOT.jar` : an example of an OBDA rewriting algorithm implemented with IMP-Logics.


The source code of *ontological-queries-rewriting-1.0-SNAPSHOT* can be seen in the package *ontological_queries_rewritting*.

The source code of IMP-Logics is freely available on https://github.com/inLabFIB/imp-logics

## Try demo

All set, you can now open the script `imp-logics-demo-script.ipynb` in the jupyter notebook environment and follow the
demo steps.